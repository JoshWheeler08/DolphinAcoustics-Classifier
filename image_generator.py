# Generate the spectrograms for the wav files
# https://stackoverflow.com/questions/44787437/how-to-convert-a-wav-file-to-a-spectrogram-in-python3

import os
import sys
import shutil
import matplotlib.pyplot as plt
import librosa
import librosa.display

def save_spectrogram_image(file_path, image_name, image_folder_name, sampling_rate=192000, fft_value=512):
  """<insert here>"""
  x, sr = librosa.load(file_path, sr=sampling_rate)
  # Drawing image
  X = librosa.stft(x, n_fft=512) # Applying fourier transform
  Xdb = librosa.amplitude_to_db(abs(X)) # Converts amplitude spectrogram to dB-scaled spec
  fig = plt.figure(frameon=False)
  fig.set_size_inches(14,5)
  ax = plt.Axes(fig, [0., 0., 1., 1.])
  ax.set_axis_off()
  fig.add_axes(ax)
  librosa.display.specshow(Xdb, sr=sr, x_axis='time', y_axis='hz')

  #Saving image
  fig.savefig(os.path.join(image_folder_name, image_name + ".png"))
  plt.close(fig)

# FS-dependent code
def find_clips_moby(root_path):
  """<insert here>"""
  for file_name in os.listdir(root_path):
    yield file_name

#Handles Storage
def create_storage_for_images(directory_to_store_images):
  """<insert here>"""
  if os.path.exists(directory_to_store_images):
    shutil.rmtree(directory_to_store_images)
  os.makedirs(directory_to_store_images)

def get_wav_paths(root_path):
    directories = []
    for directory_name in os.listdir(root_path):
        directories.append(directory_name)
    return directories

def main():
    """<insert here> """
    base_to_hdd = '/run/media/joshwheeler/Elements/DolphinVIPFiles/MobySoundClips/'
    #Path names
    root_path_for_wavs = [base_to_hdd + 'common/', base_to_hdd + 'bottlenose/', base_to_hdd + 'melonhead/'] 
    #where to find all folders with wavs
    root_path_for_images = [base_to_hdd + 'images/common/', base_to_hdd + 'images/bottlenose/', base_to_hdd + 'images/melon-headed/']
    for index, path_to_dolphin_clips in enumerate(root_path_for_wavs):
        directories_to_get_wavs_from = get_wav_paths(path_to_dolphin_clips)
        for directory_name in directories_to_get_wavs_from:
            image_folder_name = os.path.join(root_path_for_images[index] + directory_name)
            #Run image generation
            create_storage_for_images(image_folder_name)
            for clip_name in find_clips_moby(os.path.join(root_path_for_wavs[index], directory_name)):
                print("Starting : " + clip_name, end="", flush=True)
                name_without_extension = clip_name.split('.')[0] #Removing .wav from the clip name
                save_spectrogram_image(os.path.join(root_path_for_wavs[index], directory_name, clip_name), name_without_extension, image_folder_name)
                print(" [Done] ")
            print("All images have been created for : " + directory_name)
        print("--------- Finished dolphin species " + (index + 1) + "---------")

if __name__ == "__main__":
    main()