# This is prototype
# TODO Initialize image file's name from document's path

# Initialize .obj file's code
# TODO Currently, mtllib, g, usemtl keywords are using the name of the prototype image file.
objText = """
mtllib Test.mtl

#     (3)---------------------(4)
#      |                       |
#      |                       |
#      |        texture        |
#      |                       |
#      |                       |
#     (1)---------------------(2)

v 0.0 0.0 0.0
v 1.0 0.0 0.0
v 0.0 1.0 0.0
v 1.0 1.0 0.0

vt 0.0 0.0
vt 1.0 0.0
vt 0.0 1.0
vt 1.0 1.0

g Test
usemtl picture
f 1/1 2/2 3/3 4/4
"""

# Initialize .mtl File's code
# TODO Currently, newmtl, map_Kd keywords are using the name of the prototype image file.
mtlText = """
newmtl face
illum 2
Ka 0.0000 0.0000 0.0000
Kd 0.0000 0.0000 0.0000
Ks 0.0000 0.0000 0.0000
map_Kd Test.png
"""



# Create .obj, .mtl file
def createOBJ():
    file = open('test.obj', 'w')
    file.write(objText)
    file.close()

def createMTL():
    file = open('test.mtl', 'w')
    file.write(mtlText)
    file.close()

def createFile():
    createOBJ()
    createMTL()

if __name__ == "__main__":
    createFile()