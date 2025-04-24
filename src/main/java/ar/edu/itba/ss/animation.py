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
    i = 0
    events_per_time = {}
    
    while i < len(lines):
        line = lines[i].strip()
        
        if line.startswith("time: "):
            time = line.replace("time: ", "").strip()
            time_str = str(time)
            
            if time_str not in events_per_time:
                events_per_time[time_str] = []
                
            i += 1
            
            while i < len(lines) - 1:
                next_line = lines[i].strip()
                if next_line.startswith("time: ") or not next_line:
                    break
                    
                if i + 1 >= len(lines):
                    break
                
                particle1_info = lines[i].strip().split()
                particle2_info = lines[i + 1].strip().split()
                
                if len(particle2_info) == 1 and (particle2_info[0] == "wall" or particle2_info[0] == "obstacle"):
                    if len(particle1_info) == 3:  
                        try:
                            particle_id = int(particle1_info[0])
                            vx = float(particle1_info[1])
                            vy = float(particle1_info[2])
                            
                            events_per_time[time_str].append({
                                'time': time,
                                'type': 'wall',
                                'particle_id': particle_id,
                                'vx': vx,
                                'vy': vy,
                                'border_type': particle2_info[0]
                            })
                        except ValueError:
                            pass
                    i += 2
                elif len(particle1_info) == 3 and len(particle2_info) == 3:  
                    try:
                        particle1_id = int(particle1_info[0])
                        vx1 = float(particle1_info[1])
                        vy1 = float(particle1_info[2])
                        particle2_id = int(particle2_info[0])
                        vx2 = float(particle2_info[1])
                        vy2 = float(particle2_info[2])
                        events_per_time[time_str].append({
                            'time': time,
                            'type': 'particle',
                            'particle1_id': particle1_id,
                            'vx1': vx1,
                            'vy1': vy1,
                            'particle2_id': particle2_id,
                            'vx2': vx2,
                            'vy2': vy2
                        })
                    except ValueError:
                        pass
                    i += 2
                else:
                    # Skip malformed lines
                    i += 1
        else:
            # Skip any other type of line and move to the next one
            i += 1
    
    for time_str in events_per_time:
        if events_per_time[time_str]:  
            events.append({
                'time': float(time_str),
                'events': events_per_time[time_str]
            })
    
    events.sort(key=lambda x: float(x['time']))
                
    return events

def animate_particles(particles_file, events_file, output_file=None, show_animation=True, fps=500, min_frames_per_event=5):
    board_diameter, obstacle_radius, particles = read_initial_conditions(particles_file)
    events_data = read_simulation_events(events_file)
    
    current_time = 0.0
    
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

    initial_particles = []
    for p in particles:
        initial_particles.append(p.copy())
    
    if events_data:
        total_simulation_time = float(events_data[-1]['time'])
    else:
        total_simulation_time = 10.0  
    
    min_total_frames = len(events_data) * min_frames_per_event
    
    # checking if this works
    fps_based_frames = int(total_simulation_time * fps) + 1
    
    total_frames = max(fps_based_frames, min_total_frames)
    
    if total_frames > 1:
        time_per_frame = total_simulation_time / (total_frames - 1)
    else:
        time_per_frame = 0.01 
    
    frame_times = [i * time_per_frame for i in range(total_frames)]
    
    event_timestamps = [float(event_group['time']) for event_group in events_data]
    
    def reset_particles():
        for i, p in enumerate(particles):
            for key, value in initial_particles[i].items():
                p[key] = value
    
    def update_particle_velocities(event):
        if event['type'] == 'particle':
            for p in particles:
                if p['id'] == event['particle1_id']:
                    p['vx'] = event['vx1']
                    p['vy'] = event['vy1']
                elif p['id'] == event['particle2_id']:
                    p['vx'] = event['vx2']
                    p['vy'] = event['vy2']
        else:  
            for p in particles:
                if p['id'] == event['particle_id']:
                    p['vx'] = event['vx']
                    p['vy'] = event['vy']
    
    def update_particle_positions(time_delta):
        for p in particles:
            p['x'] += p['vx'] * time_delta
            p['y'] += p['vy'] * time_delta
    
    def update(frame):
        current_time = frame_times[frame]
        
        last_event_index = -1
        for i, event_time in enumerate(event_timestamps):
            if event_time <= current_time:
                last_event_index = i
            else:
                break
        
        # Reset particles to initial state and replay all events up to the current time
        reset_particles()
        
        # Apply all events up to last_event_index
        if last_event_index >= 0:
            for i in range(last_event_index + 1):
                event_group = events_data[i]
                event_time = float(event_group['time'])
                
                for event in event_group['events']:
                    update_particle_velocities(event)
                
                # If this isn't the last event we're applying, move particles to the next event time
                if i < last_event_index:
                    next_event_group = events_data[i + 1]
                    next_event_time = float(next_event_group['time'])
                    time_delta = next_event_time - event_time
                    update_particle_positions(time_delta)
                else:
                    # For the last event group, move particles forward to the current frame time
                    time_delta = current_time - event_time
                    update_particle_positions(time_delta)
        
        for i, (particle, circle) in enumerate(zip(particles, particle_circles)):
            circle.center = (particle['x'], particle['y'])
        
        time_text.set_text(f'Time: {current_time:.6f}, Event: {last_event_index + 1}')
        
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

    if len(sys.argv) > 1:
        particles_file_path = sys.argv[1]
    else:
        particles_file_path = "output.txt"

    #print(f"L = {board_diameter}, R = {obstacle_radius}")
    #for particle in particles:
    #    print(f"{particle}\n")

    #events = read_simulation_events("events.txt")
    #for event in events:
    #    print(event)
    
    animate_particles('particles.txt', 'events.txt', 'animation.gif', True)

