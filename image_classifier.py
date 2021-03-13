import matplotlib.pyplot as plt
import numpy as np
import os
import PIL # For working with images in Python
import pathlib # Used to interact with files

import tensorflow as tf
import tensorflow_hub as hub 
from tensorflow import keras
from tensorflow.keras import layers # For specifying the type of layer (Dense)
from tensorflow.keras.models import Sequential 
# ^ Allows you to create models layer by layer

def main():
    print("--------------------- Content ---------------------")

    IMAGE_SHAPE = (244,244) # Input shape for MobileNetV2
    TRAINING_DATA_DIR = '/run/media/joshwheeler/Elements/DolphinVIPFiles/images_dataset/train'
    datagen_kwargs = dict(rescale=1./255, validation_split=.20) # Normalisation
    
    valid_datagen = tf.keras.preprocessing.image.ImageDataGenerator(**datagen_kwargs) #** = collects all keyword args in dict

    valid_generator = valid_datagen.flow_from_directory(
        TRAINING_DATA_DIR,
        subset="validation",
        shuffle=True,
        target_size=IMAGE_SHAPE
    )

    train_datagen = tf.keras.preprocessing.image.ImageDataGenerator(**datagen_kwargs)
    
    train_generator = train_datagen.flow_from_directory(
        TRAINING_DATA_DIR,
        subset="training",
        shuffle=True,
        target_size=IMAGE_SHAPE
    ) # Returns a directory iterator object 

   


if __name__ == "__main__":
    main()


#References:
##https://medium.com/analytics-vidhya/how-to-do-image-classification-on-custom-dataset-using-tensorflow-52309666498e