(ns quo.components.record-audio.record-audio.handlers
  (:require [oops.core :as oops]
            [quo.components.record-audio.record-audio.constants :as record-audio.constants]
            [react-native.audio-toolkit :as audio]
            [taoensso.timbre :as log]
            [utils.datetime :as datetime]))

(def get-reload-player
  (memoize
   (fn
     [[player-ref destroy-player set-playing-audio playing-timer set-audio-current-time-ms
       set-seeking-audio]]
     (fn [audio-file]
       (when @player-ref
         (destroy-player))
       (reset! player-ref
         (audio/new-player
          (or audio-file (:filename record-audio.constants/rec-options))
          {:autoDestroy                 false
           :continuesToPlayInBackground false
           :category                    audio/PLAYBACK}
          (fn []
            (set-playing-audio false)
            (when @playing-timer
              (js/clearInterval @playing-timer)
              (reset! playing-timer nil)
              (set-audio-current-time-ms 0)
              (set-seeking-audio false)))))
       (audio/prepare-player
        @player-ref
        #(log/debug "[record-audio] prepare player - success")
        #(log/error "[record-audio] prepare player - error: " %))))))

(def get-recorder-on-meter
  (memoize
   (fn
     [[recording-start-ms set-recording-length-ms reached-max-duration? locked? set-locked
       set-reviewing-audio
       idle? set-recording
       set-ready-to-lock set-ready-to-send set-ready-to-delete recorder-ref output-file reload-player
       on-reviewing-audio]]
     (fn []
       (when @recording-start-ms
         (let [now-ms             (datetime/timestamp)
               recording-duration (- now-ms @recording-start-ms)]
           (set-recording-length-ms recording-duration)
           (when (>= recording-duration record-audio.constants/max-audio-duration-ms)
             (reset! reached-max-duration? (not locked?))
             (set-reviewing-audio true)
             (reset! idle? false)
             (set-locked false)
             (set-recording false)
             (set-ready-to-lock false)
             (set-ready-to-send false)
             (set-ready-to-delete false)
             (audio/stop-recording
              @recorder-ref
              (fn []
                (reset! output-file (audio/get-recorder-file-path @recorder-ref))
                (reload-player nil)
                (log/debug "[record-audio] stop recording - success"))
              #(log/error "[record-audio] stop recording - error: " %))
             (js/setTimeout #(reset! idle? false) 1000)
             (set-recording-length-ms 0)
             (reset! recording-start-ms nil)
             (when on-reviewing-audio
               (on-reviewing-audio (audio/get-recorder-file-path @recorder-ref))))
           (log/debug "[record-audio] new recorder - on meter")))))))

(def get-reload-recorder
  (memoize
   (fn
     [[recorder-ref recorder-on-meter]]
     (fn []
       (when @recorder-ref
         (audio/destroy-recorder @recorder-ref)
         (reset! recorder-ref nil))
       (reset! recorder-ref (audio/new-recorder
                             record-audio.constants/rec-options
                             recorder-on-meter
                             #(log/debug "[record-audio] new recorder - on ended")))))))

(def get-on-start-should-set-responder
  (memoize
   (fn
     [[locked? idle? disabled? recorder-on-meter touch-timestamp touch-identifier
       reviewing-audio? record-audio-permission-granted set-recording set-playing-audio output-file
       recorder-ref recording-start-ms set-audio-current-time-ms on-start-recording
       on-request-record-audio-permission touch-active?]]
     (fn [e]
       (when-not (or locked? @idle? (nil? e) @disabled?)
         (let [pressed-record-button? (record-audio.constants/touch-inside-area?
                                       {:location-x    (oops/oget e "nativeEvent.locationX")
                                        :location-y    (oops/oget e "nativeEvent.locationY")
                                        :ignore-min-y? false
                                        :ignore-max-y? false
                                        :ignore-min-x? false
                                        :ignore-max-x? false}
                                       record-audio.constants/record-button-area)
               new-recorder           (audio/new-recorder
                                       record-audio.constants/rec-options
                                       recorder-on-meter
                                       #(log/debug "[record-audio] new recorder - on ended"))]
           (reset! touch-timestamp (oops/oget e "nativeEvent.timestamp"))
           (reset! touch-identifier (oops/oget e "nativeEvent.identifier"))
           (when-not reviewing-audio?
             (if record-audio-permission-granted
               (do
                 (when (not @idle?)
                   (set-recording pressed-record-button?))
                 (when pressed-record-button?
                   (set-playing-audio false)
                   (reset! output-file nil)
                   (reset! recorder-ref new-recorder)
                   (audio/start-recording
                    new-recorder
                    (fn []
                      (reset! recording-start-ms (datetime/timestamp))
                      (set-audio-current-time-ms 0)
                      (log/debug "[record-audio] start recording - success"))
                    #(log/error "[record-audio] start recording - error: " %))
                   (when on-start-recording
                     (on-start-recording))))
               (when on-request-record-audio-permission
                 (on-request-record-audio-permission))))
           (when record-audio-permission-granted
             (reset! touch-active? true))))
       (and (not @idle?) (not @disabled?))))))

(def get-on-responder-move
  (memoize
   (fn
     [[locked? ready-to-send? set-ready-to-send ready-to-delete? set-ready-to-delete ready-to-lock?
       set-ready-to-lock touch-identifier
       record-button-at-initial-position? recording?]]
     (fn [^js e]
       (when-not locked?
         (let [location-x              (oops/oget e "nativeEvent.locationX")
               location-y              (oops/oget e "nativeEvent.locationY")
               page-x                  (oops/oget e "nativeEvent.pageX")
               page-y                  (oops/oget e "nativeEvent.pageY")
               identifier              (oops/oget e "nativeEvent.identifier")
               moved-to-send-button?   (record-audio.constants/touch-inside-area?
                                        {:location-x    location-x
                                         :location-y    location-y
                                         :ignore-min-y? true
                                         :ignore-max-y? false
                                         :ignore-min-x? false
                                         :ignore-max-x? true}
                                        (record-audio.constants/send-button-area
                                         {:active?          ready-to-send?
                                          :reviewing-audio? false}))
               moved-to-delete-button? (record-audio.constants/touch-inside-area?
                                        {:location-x    location-x
                                         :location-y    location-y
                                         :ignore-min-y? false
                                         :ignore-max-y? true
                                         :ignore-min-x? true
                                         :ignore-max-x? false}
                                        (record-audio.constants/delete-button-area
                                         {:active?          ready-to-delete?
                                          :reviewing-audio? false}))
               moved-to-lock-button?   (record-audio.constants/touch-inside-area?
                                        {:location-x    location-x
                                         :location-y    location-y
                                         :ignore-min-y? false
                                         :ignore-max-y? false
                                         :ignore-min-x? false
                                         :ignore-max-x? false}
                                        (record-audio.constants/lock-button-area {:active?
                                                                                  ready-to-lock?}))
               moved-to-record-button? (and
                                        (record-audio.constants/touch-inside-area?
                                         {:location-x    location-x
                                          :location-y    location-y
                                          :ignore-min-y? false
                                          :ignore-max-y? false
                                          :ignore-min-x? false
                                          :ignore-max-x? false}
                                         record-audio.constants/record-button-area-big)
                                        (not= location-x page-x)
                                        (not= location-y page-y))]
           (when (= identifier @touch-identifier)
             (cond
               (and
                (or
                 (and moved-to-record-button? ready-to-lock?)
                 (and (not locked?) moved-to-lock-button? @record-button-at-initial-position?))
                (not ready-to-delete?)
                (not ready-to-send?)
                recording?)
               (set-ready-to-lock moved-to-lock-button?)
               (and
                (or
                 (and moved-to-record-button? ready-to-delete?)
                 (and moved-to-delete-button? @record-button-at-initial-position?))
                (not ready-to-lock?)
                (not ready-to-send?)
                recording?)
               (set-ready-to-delete moved-to-delete-button?)
               (and
                (or
                 (and moved-to-record-button? ready-to-send?)
                 (and moved-to-send-button? @record-button-at-initial-position?))
                (not ready-to-lock?)
                (not ready-to-delete?)
                recording?)
               (set-ready-to-send moved-to-send-button?)))))))))

(def get-on-responder-release
  (memoize
   (fn
     [[idle? reached-max-duration? touch-timestamp recording-length-ms set-recording-length-ms
       reviewing-audio? set-reviewing-audio
       set-audio-current-time-ms set-force-show-controls on-send output-file player-ref
       destroy-player on-cancel record-button-is-animating? ready-to-lock? set-ready-to-lock
       locked? set-locked on-lock ready-to-delete? set-ready-to-delete ready-to-send? set-ready-to-send
       disabled? on-reviewing-audio recorder-ref set-recording reload-player recording-start-ms
       touch-active?]]
     (fn [^js e]
       (when (and
              (not @idle?)
              (not @reached-max-duration?))
         (let [touch-area              {:location-x    (oops/oget e "nativeEvent.locationX")
                                        :location-y    (oops/oget e "nativeEvent.locationY")
                                        :ignore-min-y? false
                                        :ignore-max-y? false
                                        :ignore-min-x? false
                                        :ignore-max-x? false}
               on-record-button?       (record-audio.constants/touch-inside-area?
                                        touch-area
                                        record-audio.constants/record-button-area-big)
               on-send-button?         (record-audio.constants/touch-inside-area?
                                        touch-area
                                        (record-audio.constants/send-button-area
                                         {:active?          false
                                          :reviewing-audio? true}))
               on-delete-button?       (record-audio.constants/touch-inside-area?
                                        touch-area
                                        (record-audio.constants/delete-button-area
                                         {:active?          false
                                          :reviewing-audio? true}))
               release-touch-timestamp (oops/oget e "nativeEvent.timestamp")
               touch-timestamp-diff    (- release-touch-timestamp @touch-timestamp)
               audio-length            recording-length-ms]
           (cond
             (and reviewing-audio? on-send-button?)
             (do
               (set-reviewing-audio false)
               (set-audio-current-time-ms 0)
               (set-force-show-controls false)
               (when on-send
                 (on-send {:file-path @output-file
                           :duration  (min record-audio.constants/max-audio-duration-ms
                                           (int (audio/get-player-duration @player-ref)))}))
               (when @player-ref
                 (audio/stop-playing
                  @player-ref
                  (fn []
                    (destroy-player)
                    (log/debug "[record-audio] stop playing - success"))
                  #(log/error "[record-audio] stop playing - error: " %))))

             (and reviewing-audio? on-delete-button?)
             (do
               (set-reviewing-audio false)
               (set-audio-current-time-ms 0)
               (set-force-show-controls false)
               (destroy-player)
               (when on-cancel
                 (on-cancel)))

             (and ready-to-lock? (not @record-button-is-animating?))
             (do
               (set-locked true)
               (set-ready-to-lock false)
               (when on-lock
                 (on-lock)))

             (and (not reviewing-audio?)
                  (or on-record-button?
                      (and (not ready-to-delete?)
                           (not ready-to-lock?)
                           (not ready-to-send?))))
             (do
               (reset! disabled? (<= touch-timestamp-diff record-audio.constants/min-touch-duration))
               (js/setTimeout
                (fn []
                  (if (>= recording-length-ms record-audio.constants/min-audio-duration-ms)
                    (do (set-reviewing-audio true)
                        (reset! idle? false)
                        (when on-reviewing-audio
                          (on-reviewing-audio (audio/get-recorder-file-path @recorder-ref))))
                    (do (when on-cancel
                          (on-cancel))
                        (reset! idle? true)))
                  (set-locked false)
                  (set-recording false)
                  (set-ready-to-lock false)
                  (audio/stop-recording
                   @recorder-ref
                   (fn []
                     (reset! output-file (audio/get-recorder-file-path @recorder-ref))
                     (when (>= audio-length record-audio.constants/min-audio-duration-ms)
                       (reload-player nil))
                     (log/debug "[record-audio] stop recording - success"))
                   #(log/error "[record-audio] stop recording - error: " %))
                  (js/setTimeout #(reset! idle? false) 1000)
                  (set-recording-length-ms 0)
                  (reset! recording-start-ms nil)
                  (reset! disabled? false))
                (if (> touch-timestamp-diff record-audio.constants/min-touch-duration) 0 250)))

             (and (not locked?) (not reviewing-audio?) (not @record-button-is-animating?))
             (do
               (reset! disabled? (<= touch-timestamp-diff record-audio.constants/min-touch-duration))
               (js/setTimeout
                (fn []
                  (audio/stop-recording
                   @recorder-ref
                   (fn []
                     (cond
                       ready-to-send?
                       (when on-send
                         (on-send {:file-path (audio/get-recorder-file-path @recorder-ref)
                                   :duration  recording-length-ms}))
                       ready-to-delete?
                       (when on-cancel
                         (on-cancel)))
                     (set-recording false)
                     (set-ready-to-send false)
                     (set-ready-to-delete false)
                     (set-ready-to-lock false)
                     (reset! idle? true)
                     (js/setTimeout #(reset! idle? false) 1000)
                     (set-recording-length-ms 0)
                     (reset! recording-start-ms nil)
                     (reset! disabled? false)
                     (log/debug "[record-audio] stop recording - success"))
                   #(log/error "[record-audio] stop recording - error: " %)))
                (if (> touch-timestamp-diff record-audio.constants/min-touch-duration) 0 250)))))
         (reset! touch-active? false))
       (when @reached-max-duration?
         (reset! reached-max-duration? false))
       (reset! touch-timestamp nil)))))
