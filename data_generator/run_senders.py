import subprocess
import argparse

SCRIPT_ZERO_STATE = "send_zero_state.py"
SCRIPTS = ["send_follow.py", "send_unfollow.py", "send_update.py"]

# Parse the CLI args
parser = argparse.ArgumentParser(description = "Temporal Graph Warehouse Generator")
parser.add_argument("--init", 
                     help="Run nodes and relationships initialization.", action='store_true')
args = parser.parse_args()

if args.init:
    # Run zero-state scipt
    process_zero = subprocess.Popen(["python", SCRIPT_ZERO_STATE])
    process_zero.wait()


# Create subprocesses for each script
processes = [subprocess.Popen(["python", script]) for script in SCRIPTS]

# Wait for all processes to finish
for process in processes:
    process.wait()