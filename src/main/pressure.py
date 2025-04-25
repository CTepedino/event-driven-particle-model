import math
import sys
import matplotlib.pyplot as plt  # Importing matplotlib for plotting

def read_particles_file(particles_file):
    """Reads particles.txt and extracts particle data."""
    with open(particles_file, 'r') as f:
        lines = f.readlines()
    
    board_diameter = float(lines[0].strip())
    obstacle_radius = float(lines[1].strip())
    particles = {}
    
    for line in lines[2:]:
        parts = line.strip().split()
        particle_id = int(parts[0])
        mass = float(parts[5])
        particles[particle_id] = {'mass': mass}
    
    return board_diameter, obstacle_radius, particles

def read_output_file(output_file):
    """Reads output.txt and extracts collision events and particle states."""
    with open(output_file, 'r') as f:
        lines = f.readlines()
    
    events = []
    i = 0
    while i < len(lines):
        # Check if the line is a time line (a float value)
        try:
            time = float(lines[i].strip())  # Parse the time
            i += 1
            particle_states = []
            
            # Read particle states until we hit the collision count
            while i < len(lines) and len(lines[i].strip().split()) >= 5:
                parts = lines[i].strip().split()
                particle_states.append({
                    'id': int(parts[0]),
                    'vx': float(parts[3]),
                    'vy': float(parts[4])
                })
                i += 1
            
            # Read collision count
            collision_count = int(lines[i].strip())  # The collision count is directly written as a number
            i += 1
            collisions = []
            
            # Read collision details
            for _ in range(collision_count):
                collisions.append(lines[i].strip().split())
                i += 1
            
            # Append the event
            events.append({'time': time, 'particle_states': particle_states, 'collisions': collisions})
        except ValueError:
            # Skip invalid lines that cannot be parsed as time
            i += 1
    
    return events

def calculate_pressure(events, particles, board_diameter, obstacle_radius):
    """Calculates instantaneous pressure on walls and obstacle over time."""
    wall_circumference = math.pi * board_diameter  # Circumference of the circular container
    obstacle_circumference = 2 * math.pi * obstacle_radius
    pressures_wall = []
    pressures_obstacle = []
    times = []
    
    for i in range(len(events) - 1):  # Stop at the second-to-last event to access the next event
        event = events[i]
        next_event = events[i + 1]  # Get the next event for velocities after collision
        delta_t = next_event['time'] - event['time']  # Use the next event's time for delta_t
        impulse_wall = 0
        impulse_obstacle = 0

        for collision in event['collisions']:
            particle_id = int(collision[0])
            collision_type = collision[1]
            
            # Ignore particle-particle collisions
            if collision_type not in ['W', 'O']:
                continue
            
            particle = particles[particle_id]
            mass = particle['mass']
            
            # Find velocity before and after collision
            try:
                # Velocity before collision from the current event
                particle_state = next(p for p in event['particle_states'] if p['id'] == particle_id)
                vx_before = particle_state['vx']
                vy_before = particle_state['vy']
                
                # Velocity after collision from the next event
                next_particle_state = next(p for p in next_event['particle_states'] if p['id'] == particle_id)
                vx_after = next_particle_state['vx']
                vy_after = next_particle_state['vy']
            except (StopIteration, IndexError):
                continue
            
            # Calculate velocity differences
            delta_vx = vx_after - vx_before
            delta_vy = vy_after - vy_before

            # Calculate impulse
            impulse = mass * ((delta_vx**2 + delta_vy**2)**0.5)

            if collision_type == 'W':  # Wall collision
                impulse_wall += impulse
            elif collision_type == 'O':  # Obstacle collision
                impulse_obstacle += impulse
        
        # Calculate pressures for this interval
        pressure_wall = impulse_wall / (delta_t * wall_circumference) if delta_t > 0 else 0
        pressure_obstacle = impulse_obstacle / (delta_t * obstacle_circumference) if delta_t > 0 else 0
        
        # Append pressures and time for this event
        pressures_wall.append(pressure_wall)
        pressures_obstacle.append(pressure_obstacle)
        times.append(event['time'])  # Append the current time
    
    return times, pressures_wall, pressures_obstacle

def write_pressure_to_file(times, pressures, filename):
    """Writes the pressure data to a file."""
    with open(filename, 'w') as f:
        for time, pressure in zip(times, pressures):
            f.write(f"{time} {pressure}\n")

def smooth_pressure(times, pressures, interval):
    """Smooths the pressure data by averaging over larger time intervals."""
    smoothed_times = []
    smoothed_pressures = []
    
    start_time = times[0]
    end_time = start_time + interval
    current_pressures = []
    
    for time, pressure in zip(times, pressures):
        if time <= end_time:
            current_pressures.append(pressure)
        else:
            # Calculate the average pressure for the current interval
            if current_pressures:
                avg_pressure = sum(current_pressures) / len(current_pressures)
                smoothed_times.append((start_time + end_time) / 2)  # Midpoint of the interval
                smoothed_pressures.append(avg_pressure)
            
            # Move to the next interval
            start_time = end_time
            end_time = start_time + interval
            current_pressures = [pressure]
    
    # Handle the last interval
    if current_pressures:
        avg_pressure = sum(current_pressures) / len(current_pressures)
        smoothed_times.append((start_time + end_time) / 2)
        smoothed_pressures.append(avg_pressure)
    
    return smoothed_times, smoothed_pressures

def plot_pressure(times, pressures, ylabel):
    """Plots a single pressure graph and displays it."""
    plt.figure(figsize=(10, 6))
    plt.plot(times, pressures, label=ylabel)
    plt.xlabel("Tiempo (s)")  # Label for the x-axis
    plt.ylabel(f"{ylabel} (N/m)")  # Label for the y-axis
    plt.grid()
    plt.show()  # Display the plot in a window

if __name__ == "__main__":
    # Default file names
    default_particles_file = "particles.txt"
    default_output_file = "output.txt"
    
    # Use command-line arguments if provided, otherwise use defaults
    particles_file = sys.argv[1] if len(sys.argv) > 1 else default_particles_file
    output_file = sys.argv[2] if len(sys.argv) > 2 else default_output_file
    
    print(f"Using particles file: {particles_file}")
    print(f"Using output file: {output_file}")
    
    board_diameter, obstacle_radius, particles = read_particles_file(particles_file)
    events = read_output_file(output_file)
    times, pressures_wall, pressures_obstacle = calculate_pressure(events, particles, board_diameter, obstacle_radius)
    
    # Define the smoothing interval (e.g., 0.01 seconds)
    smoothing_interval = 0.25
    
    # Smooth the pressures
    smoothed_times_wall, smoothed_pressures_wall = smooth_pressure(times, pressures_wall, smoothing_interval)
    smoothed_times_obstacle, smoothed_pressures_obstacle = smooth_pressure(times, pressures_obstacle, smoothing_interval)
    
    # Plot smoothed pressures
    plot_pressure(smoothed_times_wall, smoothed_pressures_wall, "Presión en las paredes")
    plot_pressure(smoothed_times_obstacle, smoothed_pressures_obstacle, "Presión en el obstáculo")