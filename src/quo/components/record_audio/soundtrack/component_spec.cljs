(ns quo.components.record-audio.soundtrack.component-spec
  (:require
    [quo.components.record-audio.soundtrack.view :as soundtrack]
    [react-native.audio-toolkit :as audio]
    [test-helpers.component :as h]))

(h/describe "soundtrack component"
  (h/setup-fake-timers)

  (h/test "renders soundtrack"
    (with-redefs [audio/get-player-duration (fn [] 2000)]
      (h/render [soundtrack/soundtrack
                 {:player-ref            {}
                  :audio-current-time-ms 0}])
      (-> (h/expect (h/get-by-test-id "soundtrack"))
          (.toBeTruthy))))

  (h/test "soundtrack on-sliding-start works"
    (with-redefs [audio/get-player-duration (fn [] 2000)]
      (let [seeking-audio? (atom false)]
        (h/render [soundtrack/soundtrack
                   {:seeking-audio?            seeking-audio?
                    :set-seeking-audio         #(reset! seeking-audio? %)
                    :player-ref                {}
                    :audio-current-time-ms     0
                    :set-audio-current-time-ms #()}])
        (h/fire-event
         :on-sliding-start
         (h/get-by-test-id "soundtrack"))
        (-> (h/expect @seeking-audio?)
            (.toBe true)))))

  (h/test "soundtrack on-sliding-complete works"
    (with-redefs [audio/get-player-duration (fn [] 2000)
                  audio/seek-player         (js/jest.fn)]
      (let [seeking-audio? (atom false)]
        (h/render [soundtrack/soundtrack
                   {:seeking-audio?            seeking-audio?
                    :set-seeking-audio         #(reset! seeking-audio? %)
                    :player-ref                {}
                    :audio-current-time-ms     0
                    :set-audio-current-time-ms #()}])
        (h/fire-event
         :on-sliding-start
         (h/get-by-test-id "soundtrack"))
        (h/fire-event
         :on-sliding-complete
         (h/get-by-test-id "soundtrack")
         1000)
        (-> (h/expect @seeking-audio?)
            (.toBe false))
        (-> (h/expect audio/seek-player)
            (.toHaveBeenCalledTimes 1)))))

  (h/test "soundtrack on-value-change when seeking audio works"
    (with-redefs [audio/get-player-duration (fn [] 2000)
                  audio/seek-player         (js/jest.fn)]
      (let [seeking-audio?        (atom false)
            audio-current-time-ms (atom 0)]
        (h/render [soundtrack/soundtrack
                   {:seeking-audio?            seeking-audio?
                    :set-seeking-audio         #(reset! seeking-audio? %)
                    :player-ref                {}
                    :audio-current-time-ms     audio-current-time-ms
                    :set-audio-current-time-ms #(reset! audio-current-time-ms %)}])
        (h/fire-event
         :on-sliding-start
         (h/get-by-test-id "soundtrack"))
        (h/fire-event
         :on-value-change
         (h/get-by-test-id "soundtrack")
         1000)
        (-> (h/expect @audio-current-time-ms)
            (.toBe 1000))))))
