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
