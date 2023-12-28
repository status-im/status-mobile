(ns quo.components.record-audio.soundtrack.component-spec
  (:require
    [quo.components.record-audio.soundtrack.view :as soundtrack]
    [react-native.audio-toolkit :as audio]
    [reagent.core :as reagent]
    [test-helpers.component :as h]))

(h/describe "soundtrack component"
  (h/setup-fake-timers)

  (h/test "renders soundtrack"
    (with-redefs [audio/get-player-duration (fn [] 2000)]
      (let [player-ref            (reagent/atom {})
            audio-current-time-ms (reagent/atom 0)]
        (h/render [:f> soundtrack/f-soundtrack
                   {:player-ref            @player-ref
                    :audio-current-time-ms audio-current-time-ms}])
        (-> (h/expect (h/get-by-test-id "soundtrack"))
            (.toBeTruthy)))))

  (h/test "soundtrack on-sliding-start works"
    (with-redefs [audio/get-player-duration (fn [] 2000)]
      (let [seeking-audio?        (reagent/atom false)
            player-ref            (reagent/atom {})
            audio-current-time-ms (reagent/atom 0)]
        (h/render [:f> soundtrack/f-soundtrack
                   {:seeking-audio?        seeking-audio?
                    :player-ref            @player-ref
                    :audio-current-time-ms audio-current-time-ms}])
        (h/fire-event
         :on-sliding-start
         (h/get-by-test-id "soundtrack"))
        (-> (h/expect @seeking-audio?)
            (.toBe true)))))

  (h/test "soundtrack on-sliding-complete works"
    (with-redefs [audio/get-player-duration (fn [] 2000)
                  audio/seek-player         (js/jest.fn)]
      (let [seeking-audio?        (reagent/atom false)
            player-ref            (reagent/atom {})
            audio-current-time-ms (reagent/atom 0)]
        (h/render [:f> soundtrack/f-soundtrack
                   {:seeking-audio?        seeking-audio?
                    :player-ref            @player-ref
                    :audio-current-time-ms audio-current-time-ms}])
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
      (let [seeking-audio?        (reagent/atom false)
            player-ref            (reagent/atom {})
            audio-current-time-ms (reagent/atom 0)]
        (h/render [:f> soundtrack/f-soundtrack
                   {:seeking-audio?        seeking-audio?
                    :player-ref            @player-ref
                    :audio-current-time-ms audio-current-time-ms}])
        (h/fire-event
         :on-sliding-start
         (h/get-by-test-id "soundtrack"))
        (h/fire-event
         :on-value-change
         (h/get-by-test-id "soundtrack")
         1000)
        (-> (h/expect @audio-current-time-ms)
            (.toBe 1000))))))
