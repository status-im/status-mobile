(ns quo.components.record-audio.record-audio.view
  (:require
    [clojure.string :as string]
    [goog.string :as gstring]
    [quo.components.icon :as icons]
    [quo.components.markdown.text :as text]
    [quo.components.record-audio.record-audio.buttons.delete-button :as delete-button]
    [quo.components.record-audio.record-audio.buttons.lock-button :as lock-button]
    [quo.components.record-audio.record-audio.buttons.record-button :as record-button]
    [quo.components.record-audio.record-audio.buttons.record-button-big :as record-button-big]
    [quo.components.record-audio.record-audio.buttons.send-button :as send-button]
    [quo.components.record-audio.record-audio.constants :as record-audio.constants]
    [quo.components.record-audio.record-audio.handlers :as handlers]
    [quo.components.record-audio.record-audio.style :as style]
    [quo.components.record-audio.soundtrack.view :as soundtrack]
    [quo.context]
    [quo.foundations.colors :as colors]
    [react-native.audio-toolkit :as audio]
    [react-native.core :as rn]
    [taoensso.timbre :as log]
    [utils.datetime :as datetime]))

(defn- recording-bar
  [recording-length-ms ready-to-delete?]
  (let [theme           (quo.context/use-theme)
        fill-percentage (/ (* recording-length-ms 100) record-audio.constants/max-audio-duration-ms)]
    [rn/view {:style (style/recording-bar-container theme)}
     [rn/view {:style (style/recording-bar fill-percentage ready-to-delete? theme)}]]))

(defn- time-counter
  [recording? recording-length-ms ready-to-delete? reviewing-audio? audio-current-time-ms]
  (let [theme    (quo.context/use-theme)
        s        (quot (if recording? recording-length-ms audio-current-time-ms) 1000)
        time-str (gstring/format "%02d:%02d" (quot s 60) (mod s 60))]
    [rn/view {:style (style/timer-container reviewing-audio?)}
     (when-not reviewing-audio?
       [rn/view {:style (style/timer-circle theme)}])
     [text/text
      (merge
       {:size   :label
        :weight :semi-bold}
       (when ready-to-delete?
         {:style (style/timer-text theme)}))
      time-str]]))

(defn- play-button
  [playing-audio? set-playing-audio player-ref playing-timer set-audio-current-time-ms seeking-audio?
   set-seeking-audio
   max-duration-ms]
  (let [theme    (quo.context/use-theme)
        on-play  (fn []
                   (set-playing-audio true)
                   (reset! playing-timer
                     (js/setInterval
                      (fn []
                        (let [current-time (audio/get-player-current-time @player-ref)
                              player-state (audio/get-state @player-ref)
                              playing?     (= player-state audio/PLAYING)]
                          (when (and playing?
                                     (not seeking-audio?)
                                     (> current-time 0)
                                     (< current-time max-duration-ms))
                            (set-audio-current-time-ms current-time))
                          (when (>= current-time max-duration-ms)
                            (audio/stop-playing
                             @player-ref
                             (fn []
                               (set-playing-audio false)
                               (when @playing-timer
                                 (js/clearInterval @playing-timer)
                                 (reset! playing-timer nil)
                                 (set-audio-current-time-ms 0)
                                 (set-seeking-audio false)))
                             #(log/error "[record-audio] stop player - error: " %)))))
                      100)))
        on-pause (fn []
                   (set-playing-audio false)
                   (when @playing-timer
                     (js/clearInterval @playing-timer)
                     (reset! playing-timer nil))
                   (log/debug "[record-audio] toggle play / pause - success"))
        on-press (fn []
                   (audio/toggle-playpause-player
                    @player-ref
                    on-play
                    on-pause
                    #(log/error "[record-audio] toggle play / pause - error: " %)))]
    [rn/touchable-opacity
     {:style    (style/play-button theme)
      :on-press on-press}
     [icons/icon
      (if playing-audio? :i/pause :i/play)
      {:color (colors/theme-colors colors/neutral-100 colors/white theme)}]]))

(defn record-audio
  [{:keys [on-init on-start-recording on-send on-cancel on-reviewing-audio audio-file on-lock
           max-duration-ms on-check-audio-permissions record-audio-permission-granted
           on-request-record-audio-permission]}]
  (let [;;STATE
        [recording? set-recording]         (rn/use-state false)
        [locked? set-locked]               (rn/use-state false)
        [ready-to-send? set-ready-to-send] (rn/use-state false)
        [ready-to-lock? set-ready-to-lock] (rn/use-state false)
        [ready-to-delete?
         set-ready-to-delete]              (rn/use-state false)
        [reviewing-audio?
         set-reviewing-audio]              (rn/use-state (some? audio-file))
        [playing-audio?
         set-playing-audio]                (rn/use-state false)
        [recording-length-ms
         set-recording-length-ms]          (rn/use-state 0)
        [audio-current-time-ms
         set-audio-current-time-ms]        (rn/use-state 0)
        [seeking-audio? set-seeking-audio] (rn/use-state false)
        [force-show-controls?
         set-force-show-controls]          (rn/use-state (some? audio-file))
        ;;ATOMS
        recording-start-ms                 (rn/use-ref-atom (datetime/timestamp))
        clear-timeout                      (rn/use-ref-atom nil)
        record-button-at-initial-position? (rn/use-ref-atom true)
        record-button-is-animating?        (rn/use-ref-atom false)
        idle?                              (rn/use-ref-atom false)
        recorder-ref                       (rn/use-ref-atom nil)
        player-ref                         (rn/use-ref-atom nil)
        touch-active?                      (rn/use-ref-atom false)
        playing-timer                      (rn/use-ref-atom nil)
        output-file                        (rn/use-ref-atom audio-file)
        reached-max-duration?              (rn/use-ref-atom false)
        touch-timestamp                    (rn/use-ref-atom nil)
        touch-identifier                   (rn/use-ref-atom nil)
        disabled?                          (rn/use-ref-atom false)
        app-state-listener                 (rn/use-ref-atom nil)
        ;;HANDLERS
        destroy-player                     (rn/use-callback
                                            (fn []
                                              (audio/destroy-player @player-ref)
                                              (reset! player-ref nil)))
        reload-player                      (handlers/get-reload-player
                                            [player-ref destroy-player set-playing-audio playing-timer
                                             set-audio-current-time-ms set-seeking-audio])
        recorder-on-meter                  (handlers/get-recorder-on-meter
                                            [recording-start-ms set-recording-length-ms
                                             reached-max-duration?
                                             locked? set-locked set-reviewing-audio idle? set-recording
                                             set-ready-to-lock set-ready-to-send set-ready-to-delete
                                             recorder-ref
                                             output-file reload-player
                                             on-reviewing-audio])
        reload-recorder                    (handlers/get-reload-recorder [recorder-ref
                                                                          recorder-on-meter])
        on-start-should-set-responder      (handlers/get-on-start-should-set-responder
                                            [locked? idle? disabled? recorder-on-meter touch-timestamp
                                             touch-identifier
                                             reviewing-audio? record-audio-permission-granted
                                             set-recording set-playing-audio output-file
                                             recorder-ref recording-start-ms set-audio-current-time-ms
                                             on-start-recording
                                             on-request-record-audio-permission touch-active?])
        on-responder-move                  (handlers/get-on-responder-move
                                            [locked? ready-to-send? set-ready-to-send
                                             ready-to-delete? set-ready-to-delete
                                             ready-to-lock? set-ready-to-lock
                                             touch-identifier
                                             record-button-at-initial-position?
                                             recording?])
        on-responder-release               (handlers/get-on-responder-release
                                            [idle? reached-max-duration? touch-timestamp
                                             recording-length-ms set-recording-length-ms
                                             reviewing-audio? set-reviewing-audio
                                             set-audio-current-time-ms set-force-show-controls on-send
                                             output-file player-ref
                                             destroy-player on-cancel record-button-is-animating?
                                             ready-to-lock? set-ready-to-lock
                                             locked? set-locked on-lock ready-to-delete?
                                             set-ready-to-delete ready-to-send? set-ready-to-send
                                             disabled? on-reviewing-audio
                                             recorder-ref set-recording reload-player recording-start-ms
                                             touch-active?])]

    ;;ON MOUNT
    (rn/use-mount
     (fn []
       (when on-check-audio-permissions (on-check-audio-permissions))
       (when on-init
         (on-init
          (fn reset-recorder []
            (set-recording false)
            (set-reviewing-audio false)
            (set-locked false)
            (set-ready-to-send false)
            (set-ready-to-lock false)
            (set-ready-to-delete false)
            (set-audio-current-time-ms 0)
            (set-recording-length-ms 0)
            (set-seeking-audio false)
            (set-playing-audio false)
            (reset! touch-active? false)
            (reset! reached-max-duration? false)
            (reset! output-file nil)
            (reset! idle? false)
            (reset! record-button-is-animating? false)
            (reset! record-button-at-initial-position? true)
            (when @clear-timeout
              (js/clearTimeout @clear-timeout)
              (reset! clear-timeout nil))
            (when @playing-timer
              (js/clearInterval @playing-timer)
              (reset! playing-timer nil))
            (reload-recorder))))
       (when audio-file
         (let [filename (last (string/split audio-file "/"))]
           (reload-player filename)))
       (reset! app-state-listener
         (.addEventListener rn/app-state
                            "change"
                            #(when (= % "background")
                               (set-playing-audio false))))))
    ;;ON UNMOUNT
    (rn/use-unmount #(.remove @app-state-listener))

    [rn/view
     {:style          style/bar-container
      :pointer-events :box-none}
     (when reviewing-audio?
       [:<>
        [play-button playing-audio? set-playing-audio player-ref playing-timer set-audio-current-time-ms
         seeking-audio? set-seeking-audio max-duration-ms]
        [soundtrack/soundtrack
         {:audio-current-time-ms     audio-current-time-ms
          :set-audio-current-time-ms set-audio-current-time-ms
          :player-ref                @player-ref
          :seeking-audio?            seeking-audio?
          :set-seeking-audio         set-seeking-audio
          :max-audio-duration-ms     max-duration-ms}]])
     (when (or recording? reviewing-audio?)
       [time-counter recording? recording-length-ms ready-to-delete? reviewing-audio?
        audio-current-time-ms])
     (when recording?
       [recording-bar recording-length-ms ready-to-delete?])
     [rn/view
      {:test-ID                       "record-audio"
       :style                         style/button-container
       :hit-slop                      {:top -70 :bottom 0 :left 0 :right 0}
       :pointer-events                :box-only
       :on-start-should-set-responder on-start-should-set-responder
       :on-responder-move             on-responder-move
       :on-responder-release          on-responder-release}
      [delete-button/delete-button recording? ready-to-delete? reviewing-audio? force-show-controls?]
      [lock-button/lock-button recording? ready-to-lock? locked?]
      [send-button/send-button recording? ready-to-send? reviewing-audio? force-show-controls?]
      [record-button-big/record-button-big
       {:recording?                         recording?
        :set-recording                      set-recording
        :ready-to-send?                     ready-to-send?
        :set-ready-to-send                  set-ready-to-send
        :ready-to-lock?                     ready-to-lock?
        :set-ready-to-lock                  set-ready-to-lock
        :ready-to-delete?                   ready-to-delete?
        :set-ready-to-delete                set-ready-to-delete
        :record-button-is-animating?        record-button-is-animating?
        :record-button-at-initial-position? record-button-at-initial-position?
        :locked?                            locked?
        :set-locked                         set-locked
        :reviewing-audio?                   reviewing-audio?
        :recording-length-ms                recording-length-ms
        :set-recording-length-ms            set-recording-length-ms
        :clear-timeout                      clear-timeout
        :touch-active?                      touch-active?
        :recorder-ref                       recorder-ref
        :reload-recorder-fn                 reload-recorder
        :idle?                              idle?
        :on-send                            on-send
        :on-cancel                          on-cancel}]
      [record-button/record-button recording? reviewing-audio?]]]))
