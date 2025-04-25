import matplotlib.pyplot as plt
import os

def read_simulation_events_for_first_time_obstacle_collisions(filename):
    with open(filename, 'r') as f:
        lines = f.readlines()
    
    N = int(lines[0].strip())
    line_index = 1

    collision_counter = 0
    times = []
    particles = []
    collision_counts = []
    
    while line_index < len(lines):
        time_str = float(lines[line_index].strip())
        
        line_index += N+1
        collisions = int(lines[line_index].strip())
        for collision_index in range(1,collisions+1):
            collision_line = lines[line_index+collision_index].strip().split()
            if collision_line[1] == 'O' and collision_line[0] not in particles:
                if collision_counter % 20 == 0:
                    particles.append(collision_line[0])
                    times.append(time_str)
                    collision_counts.append(collision_counter+1)
                collision_counter += 1
        line_index += collisions + 1
        if collision_counter > 200:
            break
    
    return times, collision_counts

def read_simulation_events_for_independent_obstacle_collisions(filename):
    with open(filename, 'r') as f:
        lines = f.readlines()
    
    N = int(lines[0].strip())
    line_index = 1

    collision_counter = 0
    times = []
    particles = []
    collision_counts = []
    
    while line_index < len(lines):
        time_str = float(lines[line_index].strip())
        
        line_index += N+1
        collisions = int(lines[line_index].strip())
        for collision_index in range(1,collisions+1):
            collision_line = lines[line_index+collision_index].strip().split()
            if collision_line[1] == 'O':
                collision_counter += 1
                if collision_counter % 20 == 0:
                    particles.append(collision_line[0])
                    times.append(time_str)
                    collision_counts.append(collision_counter+1)
        line_index += collisions + 1
        if collision_counter > 200:
            break    

    return times, collision_counts

def scatter_collisions(times, collision_counts, first_time = False):
    os.makedirs('observables', exist_ok=True)
    plt.scatter(times, collision_counts, linestyle='-', color='blue')
    plt.xlabel('Tiempo (s)')
    plt.ylabel('Nro. de choque')
    plt.grid(True, linestyle='--', alpha=0.7)

    if first_time:
        output_path = os.path.join('observables', f'first_time_collisions_vs_time.png')
    else:
        output_path = os.path.join('observables', f'independent_collisions_vs_time.png')

    plt.savefig(output_path, dpi=300, bbox_inches='tight')
    plt.close()

if __name__ == '__main__':
    # at least 15000 collisions
    times, collision_counts = read_simulation_events_for_first_time_obstacle_collisions("output.txt")
    scatter_collisions(times, collision_counts, True)

    independent_times, independent_collision_times = read_simulation_events_for_independent_obstacle_collisions("output.txt")
    scatter_collisions(independent_times, independent_collision_times, False)
