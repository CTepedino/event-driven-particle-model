import os
import sys
import matplotlib.pyplot as plt
import matplotlib.animation as animation
from matplotlib.collections import EllipseCollection
from matplotlib.animation import PillowWriter

def read_initial_conditions(filename):
    with open(filename, 'r') as f:
        lines = f.readlines()
    
    board_diameter = float(lines[0].strip())
    obstacle_radius = float(lines[1].strip())
    
    particles = []
    
    for i in range(2, len(lines)):
        if lines[i].strip(): 
            parts = lines[i].strip().split()
            if len(parts) >= 7:  
                id_, x, y, vx, vy, mass, radius = map(float, parts)
                particles.append({
                    'id': int(id_),
                    'x': x,
                    'y': y,
                    'vx': vx,
                    'vy': vy,
                    'mass': mass,
                    'radius': radius
                })
    
    return board_diameter, obstacle_radius, particles

def read_simulation_events(filename):
    with open(filename, 'r') as f:
        lines = f.readlines()
    
    events = []
    N = int(lines[0].strip())
    i = 1
    events_per_time = {}

    
    while i < len(lines):
        line = lines[i].strip()
        
        time_str = line
        
        if time_str not in events_per_time:
            events_per_time[time_str] = []
            
        i += 1
        
        for j in range(N):
            particle_info = lines[i+j].strip().split()
            
            if len(particle_info) == 5:  
                try:
                    particle_id = int(particle_info[0])
                    x = float(particle_info[1])
                    y = float(particle_info[2])
                    vx = float(particle_info[3])
                    vy = float(particle_info[4])
                    
                    events_per_time[time_str].append({
                        'particle_id': particle_id,
                        'x': x,
                        'y': y,
                        'vx': vx,
                        'vy': vy,
                    })
                except ValueError:
                    pass
        i += N

    for time_str in events_per_time:
        if events_per_time[time_str]:  
            events.append({
                'time': float(time_str),
                'events': events_per_time[time_str]
            })
    
    return events

def animate_particles(particles_file, events_file, output_file=None, show_animation=True, fps=500, min_frames_per_event=5):
    board_diameter, obstacle_radius, particles = read_initial_conditions(particles_file)
    events_data = read_simulation_events(events_file)
    
    fig, ax = plt.subplots(figsize=(8, 8))
    extra_space = 1.25
    ax.set_xlim(-board_diameter * extra_space/2, board_diameter * extra_space/2)
    ax.set_ylim(-board_diameter * extra_space/2, board_diameter * extra_space/2)
    ax.set_aspect('equal')
    ax.set_title("Particle Simulation")
    
    external_border = plt.Circle((0, 0), board_diameter/2, fill=False, color='black', linestyle='-', linewidth=4)
    ax.add_patch(external_border)
    
    obstacle = plt.Circle((0, 0), obstacle_radius, fill=False, color='red', linestyle='-', linewidth=2)
    ax.add_patch(obstacle)
    
    particle_circles = []
    particle_color = 'blue'  
    
    for particle in particles:
        circle = plt.Circle((particle['x'], particle['y']), particle['radius'], 
                           fill=True, color=particle_color, alpha=0.6)
        particle_circles.append(circle)
        ax.add_patch(circle)
    
    time_text = ax.text(-board_diameter * 1.1/2 , board_diameter * 1.1/2 , 'Time: 0.00, Event: 0', fontsize=12)

    total_frames = len(events_data) + (len(events_data)- 1)* (min_frames_per_event-1)
    
    frame_times = []
    for i in range(min_frames_per_event):
        frame_times.append(i * (events_data[0]['time'] / min_frames_per_event ))

    for index in range(len(events_data) - 1):
        current_event = events_data[index]['time']
        next_event = events_data[index+1]['time']
        delta_time = (next_event - current_event) / min_frames_per_event 
        for i in range(min_frames_per_event):
            frame_times.append(current_event + delta_time * i)

    frame_times.append(events_data[-1]['time'])
    
    event_timestamps = [float(event_group['time']) for event_group in events_data]
    
    def update_particle_velocities(event):
        for p in particles:
            if p['id'] == event['particle_id']:
                p['vx'] = event['vx']
                p['vy'] = event['vy']
    
    def update_particle_positions(event):
        for p in particles:
            if p['id'] == event['particle_id']:
                p['x'] = event['x']
                p['y'] = event['y']

    def update_particle_positions_between_events(time_delta):
        for p in particles:
            p['x'] += p['vx'] * time_delta
            p['y'] += p['vy'] * time_delta
    
    def update(frame):
        current_time = frame_times[frame]
        
        is_in_between = True
        collitions = 0
        for index, event_time in enumerate(event_timestamps):
            if current_time == event_time:
                for event in events_data[index]['events']:
                    update_particle_velocities(event)
                    update_particle_positions(event)
                    is_in_between = False

            if current_time > event_time:
                collitions = index 
                break

        if is_in_between:
            if frame > 1:
                time_delta = frame_times[frame] - frame_times[frame-1] 
            else:
                time_delta = frame_times[1]
        
            update_particle_positions_between_events(time_delta)

        for (particle, circle) in zip(particles, particle_circles):
            circle.center = (particle['x'], particle['y'])
        
        time_text.set_text(f'Time: {current_time:.6f}, Collitions: {collitions}')
        
        return particle_circles + [time_text]
    
    ani = animation.FuncAnimation(fig, update, frames=total_frames, 
                                  interval=100/fps, blit=True)

    if output_file:
        try:
            writer = PillowWriter(fps=fps)
            ani.save(output_file, writer=writer)
        except Exception as e:
            try:
                ani.save(output_file, fps=fps)
            except Exception as e:
                print(f"Error saving animation: {e}")
    
    if show_animation:
        plt.show()
        
    return ani    


if __name__ == "__main__":
    os.makedirs('animations', exist_ok=True)

    #events = read_simulation_events("output.txt")
    #for event in events:
    #    print(event)
    
    animate_particles('particles.txt', 'output.txt', 'animation.gif', True)

