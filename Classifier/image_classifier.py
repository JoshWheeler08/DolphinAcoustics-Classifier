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

    IMAGE_SHAPE = (1400,500)
    
    TRAINING_DATA_DIR = '/run/media/joshwheeler/Elements/DolphinVIPFiles/images_dataset/train'
   
    valid_datagen = tf.keras.preprocessing.image.ImageDataGenerator(
        rescale=1./255,
        validation_split=.20
    ) #https://stackoverflow.com/questions/42443936/keras-split-train-test-set-when-using-imagedatagenerator

    train_generator = valid_datagen.flow_from_directory(
        TRAINING_DATA_DIR,
        subset="training",
        shuffle=True,
        target_size=IMAGE_SHAPE
    )

    validation_generator = valid_datagen.flow_from_directory(
        TRAINING_DATA_DIR,
        subset="validation",
        shuffle=True,
        target_size=IMAGE_SHAPE
    )
    
    #print(train_generator[0][1].shape)
    #return

    # Visualise the data
    images = iter(train_generator[0][0]) 
    labels = iter(train_generator[0][1])
    classes = train_generator.class_indices.keys()
    #print(classes)

    fig, ax = plt.subplots(2,2)
    for i in range(2):
        for j in range(2):
            ax[i][j].imshow(next(images))
            label_index = np.where(next(labels)==1)[0][0]
            ax[i][j].set_xlabel(classes[0])
    plt.show()
    


   


if __name__ == "__main__":
    main()


#References:
##https://medium.com/analytics-vidhya/how-to-do-image-classification-on-custom-dataset-using-tensorflow-52309666498e