# Danger_Detect
### 청각장애인을 위한 위험 인지 안내 어플리케이션

------------------------

![desc](https://user-images.githubusercontent.com/48959435/64043394-45a21700-cb9f-11e9-938e-f6e2937993f8.JPG)

------------------------

#### 제작 방식 : 전처리(MATLAB) - 학습(TensorFlow) - Freeze - Application(Android studio)

Dataset Download 주소
[URBAN SOUND DATASETS](https://urbansounddataset.weebly.com/urbansound8k.html)

#### class 종류 : 
* 0 = air_conditioner( 에어컨 소리 )
* 1 = car_horn( 차 경적 소리 ) <- 해당 소리에 반응
* 2 = children_playing( 아이들이 노는 소리 )
* 3 = dog_bark( 개가 짖는 소리 )
* 4 = drilling( 드릴을 사용하는 소리 )
* 5 = engine_idling( 자동차 엔진 소리 ) 
* 6 = gun_shot( 총 소리 )
* 7 = jackhammer( 착암기 소라 )
* 8 = siren( 사이렌 소리 ) <- 해당 소리에 반응
* 9 = street_music( 길거리 음악 소리 )

**10가지의 소리 중 청각 장애인에게 도움이 되는 소리들이 많지만, 특히 중요한 안전과 관련이 깊은 소리인 자동차 경적 소리와 급하게 움직이는 사이렌 소리에 반응하게 모델을 설계( 10가지의 소리를 모두 학습 후 두가지의 경우에만 진동을 울리게 동작 )**

#### 전처리(MATLAB) : 16KHz, 16Bit, 1초, Mono Channel Wav Data 필요 (안드로이드 Default 값)
1. RMS -> Max Point -> 앞,뒤로 음성 자르기
2. 시간이 부족한 데이터를 Zero Padding으로 1초짜리 음성 만들기

#### 학습(TensorFlow)
1. MFCC를 사용하여 WAV 파일을 2D 이미지화 (MFCC = 40 사용)
2. conv2d - Maxpooling - conv2d - Maxpooling - Flatten - Dense 로 이어지는 신경망 사용

#### Freeze
1.

#### Application(Android studio)
1. Application 실행 시 핸드폰에 들어오는 소리(input data)는 0.2초 마다 thread가 돌아가며 받고 audio analysis를 진행하는 thread로 던짐
2. 던져진 데이터를 분석하여 만약 결과가 1인 경적 소리나 8인 사이렌소리라면 Main thread에 전달하고 진동(output)이 울림

