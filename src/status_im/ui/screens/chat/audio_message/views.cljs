(ns status-im.ui.screens.chat.audio-message.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require
   [goog.string :as gstring]
   [reagent.core :as reagent]
   [status-im.audio.core :as audio]
   [status-im.ui.components.react :as react]
   [re-frame.core :as re-frame]
   [status-im.i18n :as i18n]
   [quo.components.animated.pressable :as pressable]
   [status-im.native-module.core :as status]
   [status-im.ui.screens.chat.components.input :as input]
   [status-im.ui.screens.chat.components.style :as input.style]
   [status-im.ui.screens.chat.audio-message.styles :as styles]
   [status-im.ui.components.colors :as colors]
   [status-im.ui.components.animation :as anim]
   [status-im.ui.components.icons.vector-icons :as icons]
   [status-im.utils.utils :as utils.utils]
   [status-im.utils.fs :as fs]
   [status-im.utils.fx :as fx]))

;; reference db levels
(def total-silence-db -160)
(def silence-db -35)
(def max-db 0)

;; update interval for the pulsing rec button
(def metering-interval 100)

;; rec pulse animation target
(defonce visual-target-value (anim/create-value total-silence-db))
;;ensure animation finishes before next meter update
(defonce metering-anim-duration (int (* metering-interval 0.9)))

(defn update-meter [meter-data]
  (let [value (if meter-data
                (.-value ^js meter-data)
                total-silence-db)]
    (anim/start (anim/timing visual-target-value {:toValue value
                                                  :duration        metering-anim-duration
                                                  :useNativeDriver true}))))

(def base-filename "am.")
(def default-format "aac")
(def rec-options  (merge
                   audio/default-recorder-options
                   {:filename (str base-filename default-format)
                    :meteringInterval metering-interval}))

;; maximum 2 minutes of recordings time
;; to keep data under 900k
(def max-recording-ms (* 2 60 1000))

;; audio objects
(defonce recorder-ref (atom nil))
(defonce player-ref (atom nil))

(defn destroy-recorder []
  (audio/destroy-recorder @recorder-ref)
  (reset! recorder-ref nil))

(defn destroy-player []
  (audio/destroy-player @player-ref)
  (reset! player-ref nil))

;; state update callback
(defonce state-cb (atom #()))

;; max recording ms reached callback
(defonce max-recording-reached-cb (atom #()))

;; to be called when app goes in background
(defonce on-background-cb (atom #()))

(fx/defn on-background
  {:events [:audio-recorder/on-background]}
  [_]
  (when @on-background-cb
    (@on-background-cb))
  nil)

;; during recording
(defonce recording-timer (atom nil))
(defonce recording-start-ts (atom nil))
(defonce recording-backlog-ms (atom 0))

;; updates timer UI
(defn update-timer [timer]
  (let [ms (if @recording-start-ts
             (+
              (- (js/Date.now) @recording-start-ts)
              @recording-backlog-ms)
             @recording-backlog-ms)
        s (quot ms 1000)]
    (if (> ms max-recording-ms)
      (@max-recording-reached-cb)
      (reset! timer (gstring/format "%d:%02d" (quot s 60) (mod s 60))))))

(defn reset-timer [timer]
  (reset! timer "0:00")
  (reset! recording-backlog-ms 0))

(defn animate-buttons [rec? show-ctrl? {:keys [rec-button-anim-value ctrl-buttons-anim-value]}]
  (anim/start
   (anim/parallel
    [(anim/timing rec-button-anim-value {:toValue         (if rec? 1 0)
                                         :duration        100
                                         :useNativeDriver true})
     (anim/timing ctrl-buttons-anim-value {:toValue         (if show-ctrl? 1 0)
                                           :duration        100
                                           :useNativeDriver true})])))

(defn start-recording [{:keys [timer] :as params}]
  (if (> @recording-backlog-ms max-recording-ms)
    (@max-recording-reached-cb)
    (do
      (animate-buttons true true params)
      (reset! recording-start-ts (js/Date.now))
      (reset! recording-timer (utils.utils/set-interval #(update-timer timer) 1000))
      (audio/start-recording
       @recorder-ref
       @state-cb
       #(utils.utils/show-popup (i18n/label :t/audio-recorder-error) (:message %))))))

(defn reload-recorder []
  (when @recorder-ref
    (destroy-recorder))
  (reset! recorder-ref (audio/new-recorder rec-options #(update-meter %) @state-cb))
  ;; we skip preparation since if a recorder is prepared, player wont play
  (@state-cb))

(defn reload-player
  ([] (reload-player nil))
  ([on-success]
   (when @player-ref
     (destroy-player))
   (reset! player-ref (audio/new-player
                       (:filename rec-options)
                       {:autoDestroy false
                        :continuesToPlayInBackground false}
                       @state-cb))
   (audio/prepare-player
    @player-ref
    #(do (@state-cb) (when on-success (on-success)))
    #(utils.utils/show-popup (i18n/label :t/audio-recorder-error) (:message %)))))

(defn stop-recording [{:keys [on-success timer max-recording-reached?] :as params}]
  (when @recording-timer
    (utils.utils/clear-interval @recording-timer)
    (reset! recording-timer nil))
  (if max-recording-reached?
    (reset! recording-backlog-ms (+ @recording-backlog-ms (- (js/Date.now) @recording-start-ts)))
    (reset-timer timer))
  (audio/stop-recording
   @recorder-ref
   #(do
      (update-meter nil)
      (reload-recorder)
      (reload-player on-success))
   #(utils.utils/show-popup (i18n/label :t/audio-recorder-error) (:message %)))
  (animate-buttons false max-recording-reached? params))

(defn pause-recording [{:keys [timer] :as params}]
  (when @recording-timer
    (utils.utils/clear-interval @recording-timer)
    (reset! recording-backlog-ms (+ @recording-backlog-ms (- (js/Date.now) @recording-start-ts)))
    (reset! recording-start-ts nil)
    (reset! recording-timer nil)
    (update-timer timer))
  (audio/pause-recording
   @recorder-ref
   #(do (update-meter nil)
        (@state-cb))
   #(utils.utils/show-popup (i18n/label :t/audio-recorder-error) (:message %)))
  (animate-buttons false true params))

(defn update-state
  "update main UI state.
   general states are:
   - :recording
   - :playing
   - :ready-to-send
   - :recording-paused
   - :ready-to-record"
  [state-ref]
  (let [player-state (audio/get-state @player-ref)
        recorder-state  (audio/get-state @recorder-ref)
        output-file (or
                     (audio/get-recorder-file-path @recorder-ref)
                     (:output-file @state-ref))
        general (cond
                  (= recorder-state audio/RECORDING) :recording
                  (= player-state audio/PLAYING) :playing
                  (= player-state audio/PREPARED) :ready-to-send
                  (= recorder-state audio/PAUSED) :recording-paused
                  :else :ready-to-record)
        new-state {:general general
                   :cancel-disabled? (nil? (#{:recording :recording-paused :ready-to-send} general))
                   :output-file output-file
                   :duration (audio/get-player-duration @player-ref)}]
    (if (#{:recording :recording-paused} general)
      (status/activate-keep-awake)
      (status/deactivate-keep-awake))
    (when (not= @state-ref new-state)
      (reset! state-ref new-state))))

(defn send-audio-msessage [state-ref]
  (re-frame/dispatch [:chat/send-audio
                      (:output-file @state-ref)
                      (int (:duration @state-ref))])
  (destroy-player)
  (@state-cb))

;; rec-button-anim-value 0 => stopped, 1 => recording
(defview rec-button-view [{:keys [rec-button-anim-value state] :as params}]
  (letsubs [outer-scale (anim/interpolate visual-target-value  {:inputRange  [total-silence-db silence-db 0]
                                                                :outputRange [1 0.8 1.2]})
            inner-scale (anim/interpolate rec-button-anim-value {:inputRange  [0 1]
                                                                 :outputRange [1 0.5]})
            inner-border-radius (anim/interpolate rec-button-anim-value {:inputRange  [0 1]
                                                                         :outputRange [styles/rec-button-base-size 16]})]
    [react/touchable-highlight {:on-press #(if (= (:general @state) :recording)
                                             (pause-recording params)
                                             (start-recording params))}
     [react/view {:style styles/rec-button-container}
      [react/animated-view {:style (styles/rec-outer-circle outer-scale)}]
      [react/animated-view {:style (styles/rec-inner-circle inner-scale inner-border-radius)}]]]))

(defn- cancel-button [disabled? on-press]
  [pressable/pressable {:type     :scale
                        :disabled disabled?
                        :on-press on-press}
   [react/view {:style (input.style/send-message-button)}
    [icons/icon :main-icons/close
     {:container-style     (merge (input.style/send-message-container) {:background-color colors/gray})
      :accessibility-label :cancel-message-button
      :color               colors/white-persist}]]])

(defview audio-message-view []
  (letsubs [rec-button-anim-value (anim/create-value 0)
            ctrl-buttons-anim-value (anim/create-value 0)
            timer (reagent/atom "")
            state             (reagent/atom nil)]
    {:component-did-mount (fn []
                            (reset-timer timer)
                            (reset! state-cb #(update-state state))
                            (reset! max-recording-reached-cb #(do
                                                                (when (= (:general @state) :recording)
                                                                  (stop-recording {:rec-button-anim-value rec-button-anim-value
                                                                                   :ctrl-buttons-anim-value ctrl-buttons-anim-value
                                                                                   :timer timer
                                                                                   :max-recording-reached? true}))
                                                                (utils.utils/show-popup (i18n/label :t/audio-recorder)
                                                                                        (i18n/label :t/audio-recorder-max-ms-reached))))
                            (reset! on-background-cb #(when (= (:general @state) :recording)
                                                        (pause-recording {:rec-button-anim-value rec-button-anim-value
                                                                          :ctrl-buttons-anim-value ctrl-buttons-anim-value
                                                                          :timer timer})))
                            (reload-recorder))
     :component-will-unmount (fn []
                               (when @recording-timer
                                 (utils.utils/clear-interval @recording-timer)
                                 (reset! recording-timer nil))
                               (destroy-recorder)
                               (destroy-player)
                               (when (:output-file @state)
                                 ; possible issue if message is not yet sent?
                                 (fs/unlink (:output-file @state)))
                               (reset! state-cb nil)
                               (reset! max-recording-reached-cb nil)
                               (reset! on-background-cb nil))}
    (let [base-params {:rec-button-anim-value rec-button-anim-value
                       :ctrl-buttons-anim-value ctrl-buttons-anim-value
                       :timer timer}]
      [react/view {:style styles/container}
       [react/text {:style styles/timer} @timer]
       [react/view {:style styles/buttons-container}
        [react/animated-view {:style {:opacity ctrl-buttons-anim-value}}
         [cancel-button (:cancel-disabled? @state) #(stop-recording base-params)]]
        [rec-button-view (merge base-params {:state state})]
        [react/animated-view {:style {:opacity ctrl-buttons-anim-value}}
         [input/send-button {:on-send-press (fn [] (cond
                                                     (= :ready-to-send (:general @state))
                                                     (do
                                                       (reset-timer timer)
                                                       (animate-buttons false false base-params)
                                                       (send-audio-msessage state))

                                                     (#{:recording :recording-paused} (:general @state))
                                                     (stop-recording (merge base-params
                                                                            {:on-success
                                                                             #(send-audio-msessage state)}))))}]]]])))
