import math
import sys

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
    """Calculates pressure on walls and obstacle over time, ignoring particle-particle collisions."""
    wall_circumference = math.pi * board_diameter  # Circumference of the circular container
    obstacle_circumference = 2 * math.pi * obstacle_radius
    pressures_wall = []
    pressures_obstacle = []
    times = []
    
    for i in range(1, len(events)):  # Start from the second event to calculate delta_t
        event = events[i]
        prev_event = events[i - 1]
        delta_t = event['time'] - prev_event['time']
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
            prev_velocity = next(p for p in prev_event['particle_states'] if p['id'] == particle_id)
            curr_velocity = next(p for p in event['particle_states'] if p['id'] == particle_id)
            
            delta_vx = curr_velocity['vx'] - prev_velocity['vx']
            delta_vy = curr_velocity['vy'] - prev_velocity['vy']
            impulse = mass * ((delta_vx**2 + delta_vy**2)**0.5)
            
            if collision_type == 'W':  # Wall collision
                impulse_wall += impulse
            elif collision_type == 'O':  # Obstacle collision
                impulse_obstacle += impulse
        
        # Calculate pressures
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
    
    # Write pressures to separate files
    write_pressure_to_file(times, pressures_wall, "wall_pressure.txt")
    write_pressure_to_file(times, pressures_obstacle, "obstacle_pressure.txt")