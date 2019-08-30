clear all; close all; clc;
% Use Fourier series analysis to generate the magnitude and phase plots of 
% the EEG Signal 
% Sample frequency (given) 

path_name="C:\Users\babbu\OneDrive\문서\MATLAB\Akron\audio";
list=dir(path_name);
n=length(list);

k=3
    file_name=strcat(path_name,'\',list(k).name);
    [data,fs]=audioread(file_name);
    
    % 2채널 모노로 변환
    % mono_data=sum(data,2)/2;
    mono_data=data(:,1);
    % 파일이 1초보다 짧은 파일일 경우 Zero Pedding을 이용.
    if (length(mono_data)<16000)
        mono_data(length(mono_data)+1:250000,1)=0;
    end
     % 샘플레이트 16000Hz로 변환
     r_mono_data = resample(mono_data,16000,fs);
%     % 최대값 위치 찾기
%     rms_data=rms(r_mono_data,2); 
%     [i, j]= find(rms_data==max(max(rms_data)));
%     
%     % 시간축
%     N=length(r_mono_data);
%     Tt=N/16000;            % Calculate total time 
%     t=(1:N)/16000;         % Time vector for plotting 
%     
%     %complite data
%     if (i(1)-8000 >= 1) && (i(1)+8000 <= N)
%         c_data=r_mono_data(i(1)-8000:i(1)+8000,1);
%     elseif (i(1)-8000<1)
%         c_data=r_mono_data(1:16000,1);
%     elseif (i(1)+8000>N)
%         c_data=r_mono_data(N-16000:N,1);
%     end
%     filename=strcat(num2str(k),list(k).name);
%     audiowrite(filename, c_data,16000,'BitsPerSample',16)
% end
    re_mono_data = resample(mono_data,16000,fs);
    N=length(re_mono_data);