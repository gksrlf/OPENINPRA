import os
import sys
import uuid
import boto3
from urllib.parse import unquote_plus

# obj file initialize
objText = """
mtllib {}{}

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

# mtl file initialize
mtlText = """
newmtl face
illum 2
Ka 0.0000 0.0000 0.0000
Kd 0.0000 0.0000 0.0000
Ks 0.0000 0.0000 0.0000
map_Kd {}
"""

s3_client = boto3.client('s3')

def lambda_handler(event, context):
    for record in event['Records']:
        bucket = record['s3']['bucket']['name']
        key = unquote_plus(record['s3']['object']['key'])
        upload_object = key.replace('/', '')

        # initialize file name and extension
        name = upload_object[:-4]
        obj = ".obj"
        mtl = ".mtl"

        # initialize download and upload URL
        download_image = '/tmp/{}{}'.format(uuid.uuid4(), upload_object)
        upload_image = '/tmp/{}'.format(upload_object)
        upload_obj = '/tmp/{}{}'.format(name, obj)
        upload_mtl = '/tmp/{}{}'.format(name, mtl)

        s3_client.download_file(bucket, key, download_image)

        # download image data
        with open(download_image, 'rb') as image:
            data = image.read()
            image.close()

        # copy image data and build same image file
        with open(upload_image, 'wb') as image:
            image.write(data)
            image.close()

        # write code for obj file and build up
        with open(upload_obj, 'w+') as file:
            file.write(objText.format(name, mtl))
            file.close()

        # write code for mtl file and build up
        with open(upload_mtl, 'w+') as file:
            file.write(mtlText.format(upload_object))
            file.close()

        # upload files to another s3 bucket
        s3_client.upload_file(upload_image, '{}-resized'.format(bucket), key)
        s3_client.upload_file(upload_obj, '{}-resized'.format(bucket), '{}{}'.format(name, obj))
        s3_client.upload_file(upload_mtl, '{}-resized'.format(bucket), '{}{}'.format(name, mtl))