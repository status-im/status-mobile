(ns status-im.ui.screens.chat.message.audio
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.utils.utils :as utils]
            [reagent.core :as reagent]
            [goog.string :as gstring]
            [status-im.audio.core :as audio]
            [status-im.utils.fx :as fx]
            [status-im.ui.screens.chat.styles.message.audio :as style]
            [status-im.ui.components.animation :as anim]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.slider :as slider]))

(defn message-press-handlers [_]
  ;;TBI save audio file?
  )

(defonce player-ref (atom nil))
(defonce current-player-message-id (atom nil))
(defonce current-active-state-ref-ref (atom nil))
(defonce progress-timer (atom nil))

(defn start-stop-progress-timer [{:keys [state-ref progress-ref progress-anim]} start?]
  (when @progress-timer
    (utils/clear-interval @progress-timer)
    (when-not start?
      (reset! progress-timer nil)))
  (when start?
    (when @progress-timer
      (utils/clear-interval @progress-timer))
    (reset! progress-timer (utils/set-interval
                            #(when (and @state-ref (not (:slider-seeking @state-ref)))
                               (let [ct (audio/get-player-current-time @player-ref)]
                                 (reset! progress-ref ct)
                                 (when ct
                                   (anim/start (anim/timing progress-anim {:toValue         @progress-ref
                                                                           :duration        100
                                                                           :easing  (.-linear ^js anim/easing)
                                                                           :useNativeDriver true})))))
                            100))))

(defn update-state [{:keys [state-ref progress-ref progress-anim message-id seek-to-ms audio-duration-ms slider-new-state-seeking? unloaded? error]}]
  (let [player-state (audio/get-state @player-ref)
        slider-seeking (if (some? slider-new-state-seeking?)
                         slider-new-state-seeking?
                         (:slider-seeking @state-ref))
        general (cond
                  (some? error) :error
                  (or unloaded? (not= message-id @current-player-message-id)) :not-loaded
                  slider-seeking (:general @state-ref) ; persist player state at the time user started sliding
                  (= player-state audio/PLAYING) :playing
                  (= player-state audio/PAUSED) :paused
                  (= player-state audio/SEEKING) :seeking
                  (= player-state audio/PREPARED) :ready-to-play
                  :else :preparing)
        new-state {:general general
                   :error-msg error
                   :duration (cond (not (#{:preparing :not-loaded :error} general))
                                   (audio/get-player-duration @player-ref)

                                   audio-duration-ms audio-duration-ms

                                   :else (:duration @state-ref))
                   :progress-ref (or progress-ref (:progress-ref @state-ref))
                   :progress-anim (or progress-anim (:progress-anim @state-ref))
                   :slider-seeking slider-seeking

                   ; persist seek-to-ms while seeking or audio is not loaded
                   :seek-to-ms (when (or
                                      slider-seeking
                                      (#{:preparing :not-loaded :error} general))
                                 (or seek-to-ms (:seek-to-ms @state-ref)))}]
    ; update state if needed
    (when (not= @state-ref new-state)
      (reset! state-ref new-state))

    ; update progress UI on slider release
    (when (and (some? slider-new-state-seeking?) (not slider-new-state-seeking?) (some? seek-to-ms))
      (reset! (:progress-ref new-state) seek-to-ms))

    ; update progres anim value to follow the slider
    (when (and slider-seeking (some? seek-to-ms))
      (anim/set-value (:progress-anim new-state) seek-to-ms))

    ; on unload, reset values
    (when unloaded?
      (reset! (:progress-ref new-state) 0)
      (anim/set-value (:progress-anim new-state) 0))))

(defn destroy-player [{:keys [message-id reloading?]}]
  (when (and @player-ref (or reloading?
                             (= message-id @current-player-message-id)))
    (audio/destroy-player @player-ref)
    (reset! player-ref nil)
    (when @current-active-state-ref-ref
      (update-state {:state-ref @current-active-state-ref-ref :unloaded? true}))
    (reset! current-player-message-id nil)
    (reset! current-active-state-ref-ref nil)))

(defonce last-seek (atom (js/Date.now)))

(defn seek [{:keys [message-id] :as params} value immediate? on-success]
  (when (and @player-ref (= message-id @current-player-message-id))
    (let [now (js/Date.now)]
      (when (or immediate? (> (- now @last-seek) 200))
        (reset! last-seek (js/Date.now))
        (audio/seek-player
         @player-ref
         value
         #(do
            (update-state params)
            (when on-success (on-success)))
         #(update-state (merge params {:error (:message %)}))))))
  (update-state (merge params {:seek-to-ms value})))

(defn reload-player [{:keys [message-id state-ref] :as params} base64-data on-success]
  ;; to avoid reloading player while is initializing,
  ;; we go ahead only if there is no player or
  ;; if it is already prepared
  (when (or (nil? @player-ref) (audio/can-play? @player-ref))
    (when @player-ref
      (destroy-player (merge params {:reloading? true})))
    (reset! player-ref (audio/new-player
                        base64-data
                        {:autoDestroy false
                         :continuesToPlayInBackground false}
                        #(seek params 0 true nil)))
    (audio/prepare-player
     @player-ref
     #(when on-success (on-success))
     #(update-state (merge params {:error (:message %)})))
    (reset! current-player-message-id message-id)
    (reset! current-active-state-ref-ref state-ref)
    (update-state params)))

(defn play-pause [{:keys [message-id state-ref] :as params} audio]
  (if (not= message-id @current-player-message-id)
    ;; player has audio from another message, we need to reload
    (reload-player params
                   audio
                   ;; on-success: audio is loaded, do we have an existing value to seek to?
                   #(if-some [seek-time (:seek-to-ms @state-ref)]
                      ;; check seek time against real audio duration and play
                      (let [checked-seek-time (min (audio/get-player-duration @player-ref) seek-time)]
                        (seek params
                              checked-seek-time
                              true
                              (fn [] (play-pause params audio))))

                      ;; nothing to seek to, play
                      (play-pause params audio)))

    ;; loaded audio corresponds to current message we can play
    (when @player-ref
      (audio/toggle-playpause-player
       @player-ref
       #(do
          (start-stop-progress-timer params true)
          (update-state params))
       #(do
          (start-stop-progress-timer params false)
          (update-state params))
       #(update-state (merge params {:error (:message %)}))))))

(defn- play-pause-button [state-ref outgoing on-press]
  (let [color (if outgoing colors/blue colors/white-persist)]
    (if  (= (:general @state-ref) :preparing)
      [react/view {:style  (style/play-pause-container outgoing true)}
       [react/small-loading-indicator color]]
      [react/touchable-highlight {:on-press on-press}
       [icons/icon (case (:general @state-ref)
                     :playing :main-icons/pause
                     :main-icons/play)
        {:container-style     (style/play-pause-container outgoing false)
         :accessibility-label :play-pause-audio-message-button
         :color               color}]])))

(fx/defn on-background
  {:events [:audio-message/on-background]}
  [_]
  (when (and @current-active-state-ref-ref
             @@current-active-state-ref-ref)
    (update-state {:state-ref @current-active-state-ref-ref
                   :message-id @current-player-message-id}))
  nil)

(defview message-content [{:keys [audio audio-duration-ms message-id outgoing]} timestamp-view]
  (letsubs [state        (reagent/atom nil)
            progress     (reagent/atom 0)
            progress-anim (anim/create-value 0)
            width [:dimensions/window-width]]
    {:component-did-mount (fn []
                            (update-state {:state-ref state
                                           :audio-duration-ms audio-duration-ms
                                           :message-id message-id
                                           :unloaded? true
                                           :progress-ref progress
                                           :progress-anim progress-anim}))
     :component-will-unmount (fn []
                               (destroy-player {:state-ref state :message-id message-id})
                               (when (= @current-player-message-id message-id)
                                 (reset! current-active-state-ref-ref nil)
                                 (reset! current-player-message-id nil))
                               (reset! state nil))}

    (let [base-params {:state-ref state :message-id message-id :progress-ref progress :progress-anim progress-anim}]
      (if (= (:general @state) :error)
        [react/text {:style {:typography :main-medium
                             :margin-bottom 16}} (:error-msg @state)]
        [react/view (style/container width)
         [react/view style/play-pause-slider-container
          [play-pause-button state outgoing #(play-pause base-params audio)]
          [react/view style/slider-container
           [slider/animated-slider (merge (style/slider outgoing)
                                          {:minimum-value 0
                                           :maximum-value  (:duration @state)
                                           :value progress-anim
                                           :on-value-change #(seek base-params % false nil)
                                           :on-sliding-start #(seek (merge base-params {:slider-new-state-seeking? true}) % true nil)
                                           :on-sliding-complete #(seek (merge base-params {:slider-new-state-seeking? false}) % true nil)})]]]

         [react/view style/times-container
          [react/text {:style  (style/timestamp outgoing)}
           (let [time (cond
                        (or (:slider-seeking @state) (> (:seek-to-ms @state) 0)) (:seek-to-ms @state)
                        (#{:playing :paused :seeking}  (:general @state)) @progress
                        :else (:duration @state))
                 s (quot time 1000)]
             (gstring/format "%02d:%02d" (quot s 60) (mod s 60)))]
          timestamp-view]]))))
