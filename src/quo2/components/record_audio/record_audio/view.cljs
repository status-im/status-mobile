(ns quo2.components.record-audio.record-audio.view
  (:require [oops.core :as oops]
            [quo2.components.icon :as icons]
            [quo2.components.record-audio.record-audio.style :as style]
            [quo2.foundations.colors :as colors]
            [quo2.components.record-audio.soundtrack.view :as soundtrack]
            [react-native.core :as rn :refer [use-effect]]
            [reagent.core :as reagent]
            [quo2.components.markdown.text :as text]
            [goog.string :as gstring]
            [react-native.audio-toolkit :as audio]
            [taoensso.timbre :as log]
            [quo2.components.record-audio.record-audio.buttons.record-button-big :as record-button-big]
            [quo2.components.record-audio.record-audio.buttons.send-button :as send-button]
            [quo2.components.record-audio.record-audio.buttons.lock-button :as lock-button]
            [quo2.components.record-audio.record-audio.buttons.delete-button :as delete-button]
            [quo2.components.record-audio.record-audio.buttons.record-button :as record-button]
            [clojure.string :as string]))

(def ^:private min-audio-duration-ms 500)
(def ^:private max-audio-duration-ms 120000)
(def ^:private metering-interval 100)
(def ^:private base-filename "am")
(def ^:private default-format ".aac")

(def min-touch-duration 150)

(def ^:private record-button-area-big
  {:width  56
   :height 56
   :x      64
   :y      64})

(def ^:private record-button-area
  {:width  48
   :height 48
   :x      68
   :y      68})

(defn- delete-button-area
  [{:keys [active? reviewing-audio?]}]
  {:width  (cond
             active?          72
             reviewing-audio? 32
             :else            82)
   :height (if reviewing-audio? 32 56)
   :x      (cond
             active?          -16
             reviewing-audio? 36
             :else            -32)
   :y      (cond
             active?          64
             reviewing-audio? 76
             :else            70)})

(defn- lock-button-area
  [{:keys [active?]}]
  {:width  (if active? 72 100)
   :height (if active? 72 102)
   :x      -32
   :y      -32})

(defn- send-button-area
  [{:keys [active? reviewing-audio?]}]
  {:width  (if reviewing-audio? 32 56)
   :height (cond
             active?          72
             reviewing-audio? 47
             :else            92)
   :x      (if reviewing-audio? 76 32)
   :y      (cond
             active?          -16
             reviewing-audio? 76
             :else            -32)})

(defn touch-inside-area?
  [{:keys [location-x location-y ignore-min-y? ignore-max-y? ignore-min-x? ignore-max-x?]}
   {:keys [width height x y]}]
  (let [max-x (+ x width)
        max-y (+ y height)]
    (and
     (and
      (or ignore-min-x? (>= location-x x))
      (or ignore-max-x? (<= location-x max-x)))
     (and
      (or ignore-min-y? (>= location-y y))
      (or ignore-max-y? (<= location-y max-y))))))

(defn- f-recording-bar
  [recording-length-ms ready-to-delete?]
  (let [fill-percentage (/ (* recording-length-ms 100) max-audio-duration-ms)]
    [rn/view {:style (style/recording-bar-container)}
     [rn/view {:style (style/recording-bar fill-percentage ready-to-delete?)}]]))

(defn- f-time-counter
  [recording? recording-length-ms ready-to-delete? reviewing-audio? audio-current-time-ms]
  (let [s        (quot (if recording? recording-length-ms audio-current-time-ms) 1000)
        time-str (gstring/format "%02d:%02d" (quot s 60) (mod s 60))]
    [rn/view {:style (style/timer-container reviewing-audio?)}
     (when-not reviewing-audio?
       [rn/view {:style (style/timer-circle)}])
     [text/text
      (merge
       {:size   :label
        :weight :semi-bold}
       (when ready-to-delete?
         {:style (style/timer-text)}))
      time-str]]))

(defn- f-play-button
  [playing-audio? player-ref playing-timer audio-current-time-ms seeking-audio?]
  (let [on-play  (fn []
                   (reset! playing-audio? true)
                   (reset! playing-timer
                     (js/setInterval
                      (fn []
                        (let [current-time (audio/get-player-current-time @player-ref)
                              player-state (audio/get-state @player-ref)
                              playing?     (= player-state audio/PLAYING)]
                          (when (and playing? (not @seeking-audio?) (> current-time 0))
                            (reset! audio-current-time-ms current-time))))
                      100)))
        on-pause (fn []
                   (reset! playing-audio? false)
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
     {:style    (style/play-button)
      :on-press on-press}
     [icons/icon
      (if @playing-audio? :i/pause :i/play)
      {:color (colors/theme-colors colors/neutral-100 colors/white)}]]))

(defn record-audio
  [{:keys [on-init on-start-recording on-send on-cancel on-reviewing-audio
           record-audio-permission-granted
           on-request-record-audio-permission on-check-audio-permissions
           audio-file on-lock]}]
  [:f>
   ;; TODO we need to refactor this, and use :f> with defined function, currenly state is reseted each
   ;; time parent component
   ;; is re-rendered
   (fn []
     (let [recording? (reagent/atom false)
           locked? (reagent/atom false)
           ready-to-send? (reagent/atom false)
           ready-to-lock? (reagent/atom false)
           ready-to-delete? (reagent/atom false)
           reviewing-audio? (reagent/atom (some? audio-file))
           playing-audio? (reagent/atom false)
           recording-length-ms (reagent/atom 0)
           audio-current-time-ms (reagent/atom 0)
           seeking-audio? (reagent/atom false)
           force-show-controls? (reagent/atom (some? audio-file))
           clear-timeout (atom nil)
           record-button-at-initial-position? (atom true)
           record-button-is-animating? (atom false)
           idle? (atom false)
           recorder-ref (atom nil)
           player-ref (atom nil)
           touch-active? (atom false)
           recording-timer (atom nil)
           playing-timer (atom nil)
           output-file (atom audio-file)
           reached-max-duration? (atom false)
           touch-timestamp (atom nil)
           touch-identifier (atom nil)
           disabled? (atom false)
           app-state-listener (atom nil)
           rec-options
           (merge
            audio/default-recorder-options
            {:filename         (str base-filename (.now js/Date) default-format)
             :meteringInterval metering-interval})
           destroy-player
           (fn []
             (audio/destroy-player @player-ref)
             (reset! player-ref nil))
           reload-player
           (fn [audio-file]
             (when @player-ref
               (destroy-player))
             (reset! player-ref
               (audio/new-player
                (or audio-file (:filename rec-options))
                {:autoDestroy                 false
                 :continuesToPlayInBackground false
                 :category                    audio/PLAYBACK}
                (fn []
                  (reset! playing-audio? false)
                  (when @playing-timer
                    (js/clearInterval @playing-timer)
                    (reset! playing-timer nil)
                    (reset! audio-current-time-ms 0)
                    (reset! seeking-audio? false)))))
             (audio/prepare-player
              @player-ref
              #(log/debug "[record-audio] prepare player - success")
              #(log/error "[record-audio] prepare player - error: " %)))
           destroy-recorder
           (fn []
             (audio/destroy-recorder @recorder-ref)
             (reset! recorder-ref nil))
           reload-recorder
           (fn []
             (when @recorder-ref
               (destroy-recorder))
             (reset! recorder-ref (audio/new-recorder
                                   rec-options
                                   #(log/debug "[record-audio] new recorder - on meter")
                                   #(log/debug "[record-audio] new recorder - on ended"))))
           reset-recorder
           (fn []
             (reset! recording? false)
             (reset! reviewing-audio? false)
             (reset! locked? false)
             (reset! ready-to-send? false)
             (reset! ready-to-lock? false)
             (reset! ready-to-delete? false)
             (reset! audio-current-time-ms 0)
             (reset! recording-length-ms 0)
             (reset! seeking-audio? false)
             (reset! playing-audio? false)
             (reset! touch-active? false)
             (reset! reached-max-duration? false)
             (reset! output-file nil)
             (reset! idle? false)
             (reset! record-button-is-animating? false)
             (reset! record-button-at-initial-position? true)
             (when @clear-timeout
               (js/clearTimeout @clear-timeout)
               (reset! clear-timeout nil))
             (when @recording-timer
               (js/clearInterval @recording-timer)
               (reset! recording-timer nil))
             (when @playing-timer
               (js/clearInterval @playing-timer)
               (reset! playing-timer nil))
             (reload-recorder))
           on-start-should-set-responder
           (fn [^js e]
             (when-not (or @locked? @idle? (nil? e) @disabled?)
               (let [pressed-record-button? (touch-inside-area?
                                             {:location-x    (oops/oget e "nativeEvent.locationX")
                                              :location-y    (oops/oget e "nativeEvent.locationY")
                                              :ignore-min-y? false
                                              :ignore-max-y? false
                                              :ignore-min-x? false
                                              :ignore-max-x? false}
                                             record-button-area)
                     new-recorder           (audio/new-recorder
                                             rec-options
                                             #(log/debug "[record-audio] new recorder - on meter")
                                             #(log/debug "[record-audio] new recorder - on ended"))]
                 (reset! touch-timestamp (oops/oget e "nativeEvent.timestamp"))
                 (reset! touch-identifier (oops/oget e "nativeEvent.identifier"))
                 (when-not @reviewing-audio?
                   (if record-audio-permission-granted
                     (do
                       (when (not @idle?)
                         (reset! recording? pressed-record-button?))
                       (when pressed-record-button?
                         (reset! playing-audio? false)
                         (when @recording-timer
                           (js/clearInterval @recording-timer))
                         (reset! output-file nil)
                         (reset! recorder-ref new-recorder)
                         (audio/start-recording
                          new-recorder
                          (fn []
                            (reset! audio-current-time-ms 0)
                            (reset! recording-timer
                              (js/setInterval
                               (fn []
                                 (if (< @recording-length-ms max-audio-duration-ms)
                                   (reset! recording-length-ms
                                     (+ @recording-length-ms metering-interval))
                                   (do
                                     (reset! reached-max-duration? (not @locked?))
                                     (reset! reviewing-audio? true)
                                     (reset! idle? false)
                                     (reset! locked? false)
                                     (reset! recording? false)
                                     (reset! ready-to-lock? false)
                                     (reset! ready-to-send? false)
                                     (reset! ready-to-delete? false)
                                     (audio/stop-recording
                                      new-recorder
                                      (fn []
                                        (reset! output-file (audio/get-recorder-file-path
                                                             new-recorder))
                                        (reload-player nil)
                                        (log/debug "[record-audio] stop recording - success"))
                                      #(log/error "[record-audio] stop recording - error: " %))
                                     (js/setTimeout #(reset! idle? false) 1000)
                                     (js/clearInterval @recording-timer)
                                     (reset! recording-length-ms 0)
                                     (when on-reviewing-audio
                                       (on-reviewing-audio (audio/get-recorder-file-path
                                                            new-recorder))))))
                               metering-interval))
                            (log/debug "[record-audio] start recording - success"))
                          #(log/error "[record-audio] start recording - error: " %))
                         (when on-start-recording
                           (on-start-recording))))
                     (when on-request-record-audio-permission
                       (on-request-record-audio-permission))))
                 (when record-audio-permission-granted
                   (reset! touch-active? true))))
             (and (not @idle?) (not @disabled?)))
           on-responder-move
           (fn [^js e]
             (when-not @locked?
               (let [location-x              (oops/oget e "nativeEvent.locationX")
                     location-y              (oops/oget e "nativeEvent.locationY")
                     page-x                  (oops/oget e "nativeEvent.pageX")
                     page-y                  (oops/oget e "nativeEvent.pageY")
                     identifier              (oops/oget e "nativeEvent.identifier")
                     moved-to-send-button?   (touch-inside-area?
                                              {:location-x    location-x
                                               :location-y    location-y
                                               :ignore-min-y? true
                                               :ignore-max-y? false
                                               :ignore-min-x? false
                                               :ignore-max-x? true}
                                              (send-button-area
                                               {:active?          @ready-to-send?
                                                :reviewing-audio? false}))
                     moved-to-delete-button? (touch-inside-area?
                                              {:location-x    location-x
                                               :location-y    location-y
                                               :ignore-min-y? false
                                               :ignore-max-y? true
                                               :ignore-min-x? true
                                               :ignore-max-x? false}
                                              (delete-button-area
                                               {:active?          @ready-to-delete?
                                                :reviewing-audio? false}))
                     moved-to-lock-button?   (touch-inside-area?
                                              {:location-x    location-x
                                               :location-y    location-y
                                               :ignore-min-y? false
                                               :ignore-max-y? false
                                               :ignore-min-x? false
                                               :ignore-max-x? false}
                                              (lock-button-area {:active? @ready-to-lock?}))
                     moved-to-record-button? (and
                                              (touch-inside-area?
                                               {:location-x    location-x
                                                :location-y    location-y
                                                :ignore-min-y? false
                                                :ignore-max-y? false
                                                :ignore-min-x? false
                                                :ignore-max-x? false}
                                               record-button-area-big)
                                              (not= location-x page-x)
                                              (not= location-y page-y))]
                 (when (= identifier @touch-identifier)
                   (cond
                     (and
                      (or
                       (and moved-to-record-button? @ready-to-lock?)
                       (and (not @locked?) moved-to-lock-button? @record-button-at-initial-position?))
                      (not @ready-to-delete?)
                      (not @ready-to-send?)
                      @recording?)
                     (reset! ready-to-lock? moved-to-lock-button?)
                     (and
                      (or
                       (and moved-to-record-button? @ready-to-delete?)
                       (and moved-to-delete-button? @record-button-at-initial-position?))
                      (not @ready-to-lock?)
                      (not @ready-to-send?)
                      @recording?)
                     (reset! ready-to-delete? moved-to-delete-button?)
                     (and
                      (or
                       (and moved-to-record-button? @ready-to-send?)
                       (and moved-to-send-button? @record-button-at-initial-position?))
                      (not @ready-to-lock?)
                      (not @ready-to-delete?)
                      @recording?)
                     (reset! ready-to-send? moved-to-send-button?))))))
           on-responder-release
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
                     on-record-button?       (touch-inside-area?
                                              touch-area
                                              record-button-area-big)
                     on-send-button?         (touch-inside-area?
                                              touch-area
                                              (send-button-area
                                               {:active?          false
                                                :reviewing-audio? true}))
                     on-delete-button?       (touch-inside-area?
                                              touch-area
                                              (delete-button-area
                                               {:active?          false
                                                :reviewing-audio? true}))
                     release-touch-timestamp (oops/oget e "nativeEvent.timestamp")
                     touch-timestamp-diff    (- release-touch-timestamp @touch-timestamp)
                     audio-length            @recording-length-ms]
                 (cond
                   (and @reviewing-audio? on-send-button?)
                   (do
                     (reset! reviewing-audio? false)
                     (reset! audio-current-time-ms 0)
                     (reset! force-show-controls? false)
                     (when on-send
                       (on-send {:file-path @output-file
                                 :duration  (int (audio/get-player-duration @player-ref))}))
                     (when @player-ref
                       (audio/stop-playing
                        @player-ref
                        (fn []
                          (destroy-player)
                          (log/debug "[record-audio] stop playing - success"))
                        #(log/error "[record-audio] stop playing - error: " %))))
                   (and @reviewing-audio? on-delete-button?)
                   (do
                     (reset! reviewing-audio? false)
                     (reset! audio-current-time-ms 0)
                     (reset! force-show-controls? false)
                     (destroy-player)
                     (when on-cancel
                       (on-cancel)))
                   (and @ready-to-lock? (not @record-button-is-animating?))
                   (do
                     (reset! locked? true)
                     (reset! ready-to-lock? false)
                     (when on-lock
                       (on-lock)))
                   (and (not @reviewing-audio?)
                        (or on-record-button?
                            (and (not @ready-to-delete?)
                                 (not @ready-to-lock?)
                                 (not @ready-to-send?))))
                   (do
                     (reset! disabled? (<= touch-timestamp-diff min-touch-duration))
                     (js/setTimeout
                      (fn []
                        (if (>= @recording-length-ms min-audio-duration-ms)
                          (do (reset! reviewing-audio? true)
                              (reset! idle? false)
                              (when on-reviewing-audio
                                (on-reviewing-audio (audio/get-recorder-file-path @recorder-ref))))
                          (do (when on-cancel
                                (on-cancel))
                              (reset! idle? true)))
                        (reset! locked? false)
                        (reset! recording? false)
                        (reset! ready-to-lock? false)
                        (audio/stop-recording
                         @recorder-ref
                         (fn []
                           (reset! output-file (audio/get-recorder-file-path @recorder-ref))
                           (when (>= audio-length min-audio-duration-ms)
                             (reload-player nil))
                           (log/debug "[record-audio] stop recording - success"))
                         #(log/error "[record-audio] stop recording - error: " %))
                        (js/setTimeout #(reset! idle? false) 1000)
                        (js/clearInterval @recording-timer)
                        (reset! recording-length-ms 0)
                        (reset! disabled? false))
                      (if (> touch-timestamp-diff min-touch-duration) 0 250)))
                   (and (not @locked?) (not @reviewing-audio?) (not @record-button-is-animating?))
                   (do
                     (reset! disabled? (<= touch-timestamp-diff min-touch-duration))
                     (js/setTimeout
                      (fn []
                        (audio/stop-recording
                         @recorder-ref
                         (fn []
                           (cond
                             @ready-to-send?
                             (when on-send
                               (on-send {:file-path (audio/get-recorder-file-path @recorder-ref)
                                         :duration  @recording-length-ms}))
                             @ready-to-delete?
                             (when on-cancel
                               (on-cancel)))
                           (reset! recording? false)
                           (reset! ready-to-send? false)
                           (reset! ready-to-delete? false)
                           (reset! ready-to-lock? false)
                           (reset! idle? true)
                           (js/setTimeout #(reset! idle? false) 1000)
                           (js/clearInterval @recording-timer)
                           (reset! recording-length-ms 0)
                           (reset! disabled? false)
                           (log/debug "[record-audio] stop recording - success"))
                         #(log/error "[record-audio] stop recording - error: " %)))
                      (if (> touch-timestamp-diff min-touch-duration) 0 250)))))
               (reset! touch-active? false))
             (when @reached-max-duration?
               (reset! reached-max-duration? false))
             (reset! touch-timestamp nil))]
       (fn []
         (use-effect (fn []
                       (when on-check-audio-permissions
                         (on-check-audio-permissions))
                       (when on-init
                         (on-init reset-recorder))
                       (when audio-file
                         (let [filename (last (string/split audio-file "/"))]
                           (reload-player filename)))
                       (reset! app-state-listener
                         (.addEventListener rn/app-state
                                            "change"
                                            #(when (= % "background")
                                               (reset! playing-audio? false))))
                       #(.remove @app-state-listener)))
         [rn/view
          {:style          style/bar-container
           :pointer-events :box-none}
          (when @reviewing-audio?
            [:<>
             [:f> f-play-button playing-audio? player-ref playing-timer audio-current-time-ms
              seeking-audio?]
             [:f> soundtrack/f-soundtrack
              {:audio-current-time-ms audio-current-time-ms
               :player-ref            @player-ref
               :seeking-audio?        seeking-audio?}]])
          (when (or @recording? @reviewing-audio?)
            [:f> f-time-counter @recording? @recording-length-ms @ready-to-delete? @reviewing-audio?
             @audio-current-time-ms])
          (when @recording?
            [:f> f-recording-bar @recording-length-ms @ready-to-delete?])
          [rn/view
           {:test-ID                       "record-audio"
            :style                         style/button-container
            :hit-slop                      {:top    -70
                                            :bottom 0
                                            :left   0
                                            :right  0}
            :pointer-events                :box-only
            :on-start-should-set-responder on-start-should-set-responder
            :on-responder-move             on-responder-move
            :on-responder-release          on-responder-release}
           [:f> delete-button/f-delete-button recording? ready-to-delete? reviewing-audio?
            @force-show-controls?]
           [:f> lock-button/f-lock-button recording? ready-to-lock? locked?]
           [:f> send-button/f-send-button recording? ready-to-send? reviewing-audio?
            @force-show-controls?]
           [:f> record-button-big/f-record-button-big
            recording?
            ready-to-send?
            ready-to-lock?
            ready-to-delete?
            record-button-is-animating?
            record-button-at-initial-position?
            locked?
            reviewing-audio?
            recording-timer
            recording-length-ms
            clear-timeout
            touch-active?
            recorder-ref
            reload-recorder
            idle?
            on-send
            on-cancel]
           [:f> record-button/f-record-button recording? reviewing-audio?]]])))])
