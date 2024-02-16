(ns quo.components.record-audio.record-audio.component-spec
  (:require
    [quo.components.record-audio.record-audio.view :as record-audio]
    [react-native.audio-toolkit :as audio]
    [reagent.core :as reagent]
    [test-helpers.component :as h]
    [utils.datetime :as datetime]))

(h/describe "record audio component"
  (h/setup-fake-timers)

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
    (let [event    (js/jest.fn)
          on-meter (reagent/atom nil)]
      (h/render [record-audio/record-audio
                 {:on-reviewing-audio              event
                  :record-audio-permission-granted true}])
      (with-redefs [audio/new-recorder           (fn [_ on-meter-fn _]
                                                   (reset! on-meter on-meter-fn))
                    audio/start-recording        (fn [_ on-start _]
                                                   (on-start)
                                                   (js/setInterval #(@on-meter) 100))
                    audio/get-recorder-file-path (fn [] "file-path")]
        (h/fire-event
         :on-start-should-set-responder
         (h/get-by-test-id "record-audio")
         {:nativeEvent {:locationX  70
                        :locationY  70
                        :timestamp  0
                        :identifier 0}})
        (with-redefs [datetime/timestamp (fn [] (+ (.now js/Date) 1000))]
          (h/advance-timers-by-time 100)
          (h/fire-event
           :on-responder-release
           (h/get-by-test-id "record-audio")
           {:nativeEvent {:locationX  70
                          :locationY  70
                          :timestamp  200
                          :identifier 0}})
          (h/advance-timers-by-time 250)
          (-> (h/expect event)
              (.toHaveBeenCalledTimes 1))))))

  (h/test "record-audio on-send works after reviewing audio"
    (let [event    (js/jest.fn)
          on-meter (reagent/atom nil)]
      (h/render [record-audio/record-audio
                 {:on-send                         event
                  :record-audio-permission-granted true}])
      (with-redefs [audio/new-recorder           (fn [_ on-meter-fn _]
                                                   (reset! on-meter on-meter-fn))
                    audio/start-recording        (fn [_ on-start _]
                                                   (on-start)
                                                   (js/setInterval #(@on-meter) 100))
                    audio/get-recorder-file-path (fn [] "audio-file-path")
                    audio/get-player-duration    (fn [] 5000)]
        (h/fire-event
         :on-start-should-set-responder
         (h/get-by-test-id "record-audio")
         {:nativeEvent {:locationX  70
                        :locationY  70
                        :timestamp  0
                        :identifier 0}})
        (with-redefs [datetime/timestamp (fn [] (+ (.now js/Date) 1000))]
          (h/advance-timers-by-time 100)
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
                                      :duration  5000}))))))

  (h/test "record-audio on-send works after sliding to the send button"
    (let [event       (js/jest.fn)
          on-meter    (reagent/atom nil)
          last-now-ms (atom nil)
          duration-ms (atom nil)]
      (h/render [record-audio/record-audio
                 {:on-send                         event
                  :record-audio-permission-granted true}])
      (with-redefs [audio/new-recorder           (fn [_ on-meter-fn _]
                                                   (reset! on-meter on-meter-fn))
                    audio/start-recording        (fn [_ on-start _]
                                                   (on-start)
                                                   (js/setInterval #(@on-meter) 100))
                    audio/stop-recording         (fn [_ on-stop _]
                                                   (on-stop))
                    audio/get-recorder-file-path (fn [] "audio-file-path")
                    datetime/timestamp           (fn []
                                                   (let [now-ms (.now js/Date)]
                                                     (reset! last-now-ms now-ms)
                                                     now-ms))]
        (h/fire-event
         :on-start-should-set-responder
         (h/get-by-test-id "record-audio")
         {:nativeEvent {:locationX  70
                        :locationY  70
                        :timestamp  0
                        :identifier 0}})
        (with-redefs [datetime/timestamp (fn []
                                           (let [now-plus-ms  (+ (.now js/Date) 1000)
                                                 time-diff-ms (- now-plus-ms @last-now-ms)]
                                             (when-not @duration-ms
                                               (reset! duration-ms time-diff-ms))
                                             now-plus-ms))]
          (h/advance-timers-by-time 100)
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
                                      :duration  @duration-ms}))))))

  (h/test "record-audio on-cancel works after reviewing audio"
    (let [event    (js/jest.fn)
          on-meter (reagent/atom nil)]
      (h/render [record-audio/record-audio
                 {:on-cancel                       event
                  :record-audio-permission-granted true}])
      (with-redefs [audio/new-recorder    (fn [_ on-meter-fn _]
                                            (reset! on-meter on-meter-fn))
                    audio/start-recording (fn [_ on-start _]
                                            (on-start)
                                            (js/setInterval #(@on-meter) 100))]
        (h/fire-event
         :on-start-should-set-responder
         (h/get-by-test-id "record-audio")
         {:nativeEvent {:locationX  70
                        :locationY  70
                        :timestamp  0
                        :identifier 0}})
        (with-redefs [datetime/timestamp (fn [] (+ (.now js/Date) 1000))]
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
              (.toHaveBeenCalledTimes 1))))))

  (h/test "record-audio on-cancel works after sliding to the cancel button"
    (let [event    (js/jest.fn)
          on-meter (reagent/atom nil)]
      (h/render [record-audio/record-audio
                 {:on-cancel                       event
                  :record-audio-permission-granted true}])
      (with-redefs [audio/new-recorder    (fn [_ on-meter-fn _]
                                            (reset! on-meter on-meter-fn))
                    audio/start-recording (fn [_ on-start _]
                                            (on-start)
                                            (js/setInterval #(@on-meter) 100))
                    audio/stop-recording  (fn [_ on-stop _]
                                            (on-stop))]
        (h/fire-event
         :on-start-should-set-responder
         (h/get-by-test-id "record-audio")
         {:nativeEvent {:locationX  70
                        :locationY  70
                        :timestamp  0
                        :identifier 0}})
        (with-redefs [datetime/timestamp (fn [] (+ (.now js/Date) 1000))]
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
              (.toHaveBeenCalledTimes 1)))))))
