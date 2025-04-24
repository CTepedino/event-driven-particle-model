import math

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
        if lines[i].strip().replace('.', '', 1).isdigit():  # Time line
            time = float(lines[i].strip())
            i += 1
            particle_states = []
            while lines[i].strip() and not lines[i].strip().isdigit():
                parts = lines[i].strip().split()
                particle_states.append({
                    'id': int(parts[0]),
                    'vx': float(parts[3]),
                    'vy': float(parts[4])
                })
                i += 1
            collision_count = int(lines[i].strip())
            i += 1
            collisions = []
            for _ in range(collision_count):
                collisions.append(lines[i].strip().split())
                i += 1
            events.append({'time': time, 'particle_states': particle_states, 'collisions': collisions})
        else:
            i += 1
    
    return events

def calculate_pressure(events, particles, board_diameter, obstacle_radius):
    """Calculates pressure on walls and obstacle over time."""
    wall_circumference = math.pi * board_diameter  # Circumference of the circular container
    obstacle_circumference = 2 * math.pi * obstacle_radius
    pressures_wall = []
    pressures_obstacle = []
    times = []
    
    for i in range(1, len(events)):
        event = events[i]
        prev_event = events[i - 1]
        delta_t = event['time'] - prev_event['time']
        impulse_wall = 0
        impulse_obstacle = 0
        
        for collision in event['collisions']:
            particle_id = int(collision[0])
            collision_type = collision[1]
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
        
        pressures_wall.append(pressure_wall)
        pressures_obstacle.append(pressure_obstacle)
        times.append(event['time'])
    
    return times, pressures_wall, pressures_obstacle

def write_pressure_to_file(times, pressures, filename):
    """Writes the pressure data to a file."""
    with open(filename, 'w') as f:
        for time, pressure in zip(times, pressures):
            f.write(f"{time} {pressure}\n")

if __name__ == "__main__":
    particles_file = "particles.txt"
    output_file = "output.txt"
    
    board_diameter, obstacle_radius, particles = read_particles_file(particles_file)
    events = read_output_file(output_file)
    times, pressures_wall, pressures_obstacle = calculate_pressure(events, particles, board_diameter, obstacle_radius)
    
    # Write pressures to separate files
    write_pressure_to_file(times, pressures_wall, "wall_pressure.txt")
    write_pressure_to_file(times, pressures_obstacle, "obstacle_pressure.txt")