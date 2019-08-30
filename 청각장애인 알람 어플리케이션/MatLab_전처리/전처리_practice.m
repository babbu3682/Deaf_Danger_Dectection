clear all; close all; clc;

% path_name="C:\Users\babbu\OneDrive\문서\MATLAB\Akron\audio";
% list=dir(path_name);
% n=length(list);
% 
% for k=3:1:n
% 
% audioinfo(list(k).name)
% end

audioinfo('68657_nohash_1_0_0.wav')

% path_name="C:\Users\babbu\OneDrive\문서\MATLAB\Akron\audio";
% list=dir(path_name);
% n=length(list);
% 
% k=3
%     file_name=strcat(path_name,'\',list(k).name);
%     [data,fs]=audioread(file_name);
%     mono_data=data(:,1);
%     r_mono_data = resample(mono_data,16000,fs);
% 
% sound(r_mono_data,16000)