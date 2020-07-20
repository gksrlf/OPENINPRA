# OPENINPRA PROJECT
> ## TDC Project
> * **2020-07-21 upload**
> *Amazon Lambda server python code*
```python
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
```

> * **Image viewed on Android screen**
<img src="/OPENINPRA/test_image.JPG" width="50%" height="50%" alt="prototype"></img>
