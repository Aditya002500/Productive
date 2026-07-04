import os
import glob

directory = "/Users/kaushalendra/Desktop/Productive/app/src/main/java/com/example/ui/screens"

for filepath in glob.glob(os.path.join(directory, "*.kt")):
    with open(filepath, 'r') as f:
        content = f.read()

    # Replace specific background occurrences
    content = content.replace("containerColor = Color.White", "containerColor = MaterialTheme.colorScheme.surface")
    content = content.replace("containerColor = Color.White,", "containerColor = MaterialTheme.colorScheme.surface,")
    content = content.replace(".background(Color.White", ".background(MaterialTheme.colorScheme.surface")
    content = content.replace("Color.White else Color.Transparent", "MaterialTheme.colorScheme.surface else Color.Transparent")

    with open(filepath, 'w') as f:
        f.write(content)

print("Replaced Color.White with MaterialTheme.colorScheme.surface in all screens")
