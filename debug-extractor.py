import re

# Regular expression patterns to match debug print statements
patterns = [
    r'Starting processing for file: .+',
    r'Number of edges loaded: .+',
    r'Graph vertices count: .+',
    r'Graph edges count: .+',
    r'Number of matching edges: .+',
    r'Matching saved to: .+',
    r'=+\s*Found an augmenting path, updating matching',
    r'=+\s*No more augmenting paths'
]

# Read the terminal output from a text file
file_path = "/Users/rafael/Desktop/project_helper/debug-output.txt"
with open(file_path, 'r') as file:
    terminal_output = file.read()

# Find all debug print statements in the terminal output
debug_statements = []
for pattern in patterns:
    debug_statements.extend(re.findall(pattern, terminal_output))

# Print the extracted debug statements
for statement in debug_statements:
    print(statement)