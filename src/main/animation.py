import os

import matplotlib.pyplot as plt
import matplotlib.patches as patches
import matplotlib.animation as animation

if __name__ == "__main__":
    os.makedirs('animations', exist_ok=True)

    global ani
    fig, ax = plt.subplots()

    particles_file_path = "particles.txt"
    output_file_path = "output.txt"

    with open(particles_file_path, "r") as f:
        lines = f.readlines()

    boardRadius = float(lines[0].strip())/2
    obstacleRadius = float(lines[1].strip())
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

    times = []
    snapshots = []
    with open(output_file_path, "r") as f:
        lines = f.readlines()

    particle_count = int(lines[0])
    i = 1
    step = 0
    while i < len(lines):
        time = float(lines[i].strip())
        particle_lines = lines[i+1 : i+1+particle_count]

        if step % 100 == 0: #TODO: determinar steps por frame
            times.append(time)
            snapshot_particles = []
            for line in particle_lines:
                parts = line.strip().split()
                id_, x, y, vx, vy= map(float, parts)
                snapshot_particles.append({'radius': particles[int(id_)]["radius"], 'x': x, 'y': y})
            snapshots.append(snapshot_particles)


        i += 1 + particle_count
        step += 1


    board = patches.Circle((0, 0), boardRadius, fill=False, edgecolor="black", linewidth=2)
    ax.add_patch(board)

    obstacle = patches.Circle((0, 0), obstacleRadius, color='black')
    ax.add_patch(obstacle)

    particle_patches = []
    for particle in snapshots[0]:
        patch = patches.Circle((particle['x'], particle['y']), particle['radius'], color='blue')
        ax.add_patch(patch)
        particle_patches.append(patch)


    def update(frame):
        snapshot = snapshots[frame]

        for patch, particle in zip(particle_patches, snapshot):
            patch.center = (particle['x'], particle['y'])
            patch.radius = particle['radius']

        return particle_patches

    ani = animation.FuncAnimation(
        fig, update, frames=len(snapshots), interval=50, blit=True
    )

    ax.set_aspect('equal')
    ax.set_xlim(-boardRadius, boardRadius)
    ax.set_ylim(-boardRadius, boardRadius)
    plt.show()


