clear all; close all; clc;
% Use Fourier series analysis to generate the magnitude and phase plots of 
% the EEG Signal 
% Sample frequency (given) 

path_name="C:\Users\babbu\OneDrive\문서\MATLAB\Akron\audio";
list=dir(path_name);
n=length(list);

for k=3:1:n
    file_name=strcat(path_name,'\',list(k).name);
    [data,fs]=audioread(file_name);
    
    audioinfo(file_name)
    % 저장할 때, 이름 바꾸기용
    %filename=strcat('770000',num2str(k),'-1-0-0.wav');
    %filename=strcat('ky',list(k).name);
    % audiowrite(list(k).name,complite_data,16000,'BitsPerSample',16)
    % audiowrite(list(k).name,data,fs,'BitsPerSample',16)
end

% audioinfo('9031_nohash_3_4_0.wav')

fprintf('Wav 변환 완료')
