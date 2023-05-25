(ns quo2.components.record-audio.record-audio.--tests--.record-audio-component-spec
  (:require [quo2.components.record-audio.record-audio.view :as record-audio]
            [react-native.audio-toolkit :as audio]
            [test-helpers.component :as h]))

(h/describe "record audio component"
  (h/before-each
   (fn []
     (h/use-fake-timers)))

  (h/after-each
   (fn []
     (h/clear-all-timers)
     (h/use-real-timers)))

  (h/test "renders record-audio"
    (h/render [record-audio/record-audio])
    (-> (h/expect (h/get-by-test-id "record-audio"))
        (.toBeTruthy)))

  (h/test "record-audio on-start-recording works"
    (let [event (js/jest.fn)]
      (h/render [record-audio/record-audio
                 {:on-start-recording              event
                  :record-audio-permission-granted true}])
      (h/fire-event
       :on-start-should-set-responder
       (h/get-by-test-id "record-audio")
       {:nativeEvent {:locationX  70
                      :locationY  70
                      :timestamp  0
                      :identifier 0}})
      (-> (h/expect event)
          (.toHaveBeenCalledTimes 1))))

  (h/test "record-audio on-reviewing-audio works"
    (let [event (js/jest.fn)]
      (h/render [record-audio/record-audio
                 {:on-reviewing-audio              event
                  :record-audio-permission-granted true}])
      (with-redefs [audio/start-recording        (fn [_ on-start _]
                                                   (on-start))
                    audio/get-recorder-file-path (fn [] "file-path")]
        (h/fire-event
         :on-start-should-set-responder
         (h/get-by-test-id "record-audio")
         {:nativeEvent {:locationX  70
                        :locationY  70
                        :timestamp  0
                        :identifier 0}})
        (h/advance-timers-by-time 500)
        (h/fire-event
         :on-responder-release
         (h/get-by-test-id "record-audio")
         {:nativeEvent {:locationX  70
                        :locationY  70
                        :timestamp  200
                        :identifier 0}})
        (h/advance-timers-by-time 250)
        (-> (h/expect event)
            (.toHaveBeenCalledTimes 1)))))

  (h/test "record-audio on-send works after reviewing audio"
    (let [event (js/jest.fn)]
      (h/render [record-audio/record-audio
                 {:on-send                         event
                  :record-audio-permission-granted true}])
      (with-redefs [audio/start-recording        (fn [_ on-start _]
                                                   (on-start))
                    audio/get-recorder-file-path (fn [] "audio-file-path")
                    audio/get-player-duration    (fn [] 5000)]
        (h/fire-event
         :on-start-should-set-responder
         (h/get-by-test-id "record-audio")
         {:nativeEvent {:locationX  70
                        :locationY  70
                        :timestamp  0
                        :identifier 0}})
        (h/advance-timers-by-time 500)
        (h/fire-event
         :on-responder-release
         (h/get-by-test-id "record-audio")
         {:nativeEvent {:locationX  70
                        :locationY  70
                        :timestamp  200
                        :identifier 0}})
        (h/fire-event
         :on-start-should-set-responder
         (h/get-by-test-id "record-audio")
         {:nativeEvent {:locationX  70
                        :locationY  70
                        :timestamp  0
                        :identifier 0}})
        (h/advance-timers-by-time 500)
        (h/fire-event
         :on-responder-release
         (h/get-by-test-id "record-audio")
         {:nativeEvent {:locationX  80
                        :locationY  80
                        :timestamp  200
                        :identifier 0}})
        (h/advance-timers-by-time 250)
        (-> (js/expect event)
            (.toHaveBeenCalledTimes 1))
        (-> (js/expect event)
            (.toHaveBeenCalledWith {:file-path "audio-file-path"
                                    :duration  5000})))))

  (h/test "record-audio on-send works after sliding to the send button"
    (let [event (js/jest.fn)]
      (h/render [record-audio/record-audio
                 {:on-send                         event
                  :record-audio-permission-granted true}])
      (with-redefs [audio/start-recording        (fn [_ on-start _]
                                                   (on-start))
                    audio/stop-recording         (fn [_ on-stop _]
                                                   (on-stop))
                    audio/get-recorder-file-path (fn [] "audio-file-path")]
        (h/fire-event
         :on-start-should-set-responder
         (h/get-by-test-id "record-audio")
         {:nativeEvent {:locationX  70
                        :locationY  70
                        :timestamp  0
                        :identifier 0}})
        (h/advance-timers-by-time 500)
        (h/fire-event
         :on-responder-move
         (h/get-by-test-id "record-audio")
         {:nativeEvent {:locationX  80
                        :locationY  -30
                        :pageX      80
                        :pageY      -30
                        :identifier 0}})
        (h/fire-event
         :on-responder-release
         (h/get-by-test-id "record-audio")
         {:nativeEvent {:locationX  40
                        :locationY  80
                        :timestamp  200
                        :identifier 0}})
        (h/advance-timers-by-time 250)
        (-> (js/expect event)
            (.toHaveBeenCalledTimes 1))
        (-> (js/expect event)
            (.toHaveBeenCalledWith {:file-path "audio-file-path"
                                    :duration  500})))))

  (h/test "record-audio on-cancel works after reviewing audio"
    (let [event (js/jest.fn)]
      (h/render [record-audio/record-audio
                 {:on-cancel                       event
                  :record-audio-permission-granted true}])
      (with-redefs [audio/start-recording (fn [_ on-start _]
                                            (on-start))]
        (h/fire-event
         :on-start-should-set-responder
         (h/get-by-test-id "record-audio")
         {:nativeEvent {:locationX  70
                        :locationY  70
                        :timestamp  0
                        :identifier 0}})
        (h/advance-timers-by-time 500)
        (h/fire-event
         :on-responder-release
         (h/get-by-test-id "record-audio")
         {:nativeEvent {:locationX  70
                        :locationY  70
                        :timestamp  200
                        :identifier 0}})
        (h/fire-event
         :on-responder-release
         (h/get-by-test-id "record-audio")
         {:nativeEvent {:locationX  40
                        :locationY  80
                        :timestamp  200
                        :identifier 0}})
        (h/advance-timers-by-time 250)
        (-> (js/expect event)
            (.toHaveBeenCalledTimes 1)))))

  (h/test "cord-audio on-cancel works after sliding to the cancel button"
    (let [event (js/jest.fn)]
      (h/render [record-audio/record-audio
                 {:on-cancel                       event
                  :record-audio-permission-granted true}])
      (with-redefs [audio/start-recording (fn [_ on-start _]
                                            (on-start))
                    audio/stop-recording  (fn [_ on-stop _]
                                            (on-stop))]
        (h/fire-event
         :on-start-should-set-responder
         (h/get-by-test-id "record-audio")
         {:nativeEvent {:locationX  70
                        :locationY  70
                        :timestamp  0
                        :identifier 0}})
        (h/advance-timers-by-time 500)
        (h/fire-event
         :on-responder-move
         (h/get-by-test-id "record-audio")
         {:nativeEvent {:locationX  -30
                        :locationY  80
                        :pageX      -30
                        :pageY      80
                        :identifier 0}})
        (h/fire-event
         :on-responder-release
         (h/get-by-test-id "record-audio")
         {:nativeEvent {:locationX  -10
                        :locationY  70
                        :timestamp  200
                        :identifier 0}})
        (h/advance-timers-by-time 250)
        (-> (js/expect event)
            (.toHaveBeenCalledTimes 1))))))
