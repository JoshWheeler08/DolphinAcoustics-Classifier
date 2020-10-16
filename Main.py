from os import listdir

PATH_TO_WAVS = "/cs/scratch/jmw37/5th_DCL_data_bottlenose/"

PATH_TO_ANNOTATIONS = "./Annotations/bottlenose/"

def main():
     for filename in listdir(PATH_TO_ANNOTATIONS):
         print(filename)

if __name__ == "__main__":
    main()