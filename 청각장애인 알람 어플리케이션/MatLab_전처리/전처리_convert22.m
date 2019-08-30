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
    
    % 2채널 -> Mono(1채널)로 변환
    % mono_data=sum(data,2)/2;
    mono_data=data(:,1);
     % 파일이 1초보다 짧은 파일일 경우 Zero Pedding을 이용.
     detect=length(mono_data);
     % x * 16000/fs = 16000개가 나와야하므로 x는 fs보다 많아야한다.
    if (length(mono_data)<=fs)
        mono_data=[zeros(fs-(length(mono_data)+100),1); mono_data; zeros(200,1)];
    end
    % 샘플레이트 16000Hz로 변환
    re_mono_data = resample(mono_data,16000,fs);
    % 최대값 위치 찾기
    rms_data=rms(re_mono_data,2); 
    [i, j]= find(rms_data==max(max(rms_data)));
    
    % 시간축
    N=length(re_mono_data);
    Tt=N/16000;            % Calculate total time 
    t=(1:N)/16000;         % Time vector for plotting 
    
    % complite data
    % 첫번째로 Max값나오는 값을 기준으로 앞뒤로 1초만큼 짜르기.
    if (i(1)-8000 >= 1) && (i(1)+8000 <= N)
        complite_data=re_mono_data(i(1)-8000:i(1)+8000,1);
    elseif (i(1)-8000<1)
        complite_data=re_mono_data(1:16000,1);
    elseif (i(1)+8000>N)
        complite_data=re_mono_data(N-16000:N,1);
    end
    % strcat 함수는 쉼표 앞뒤의 파일명을 합쳐 준다.
    % num2str은 문자가아닌 인수로 정의한다.
    
    % 저장할 때, 이름 바꾸기용
    filename=strcat('ky',list(k).name);
    %filename=strcat('770000',num2str(k),'-1-0-0.wav');
    
    % audiowrite(list(k).name,complite_data,16000,'BitsPerSample',16)
    audiowrite(filename,complite_data,16000,'BitsPerSample',16)
end

fprintf('Wav 변환 완료')
