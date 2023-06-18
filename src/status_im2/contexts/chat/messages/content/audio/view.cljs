(ns status-im2.contexts.chat.messages.content.audio.view
  (:require ["react-native-blob-util" :default ReactNativeBlobUtil]
            [goog.string :as gstring]
            [reagent.core :as reagent]
            [react-native.audio-toolkit :as audio]
            [status-im2.contexts.chat.messages.content.audio.style :as style]
            [react-native.platform :as platform]
            [taoensso.timbre :as log]
            [quo2.foundations.colors :as colors]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [utils.re-frame :as rf]
            [utils.i18n :as i18n]))

(def ^:const media-server-uri-prefix "https://localhost:")
(def ^:const audio-path "/messages/audio")
(def ^:const uri-param "?messageId=")

(defonce active-players (atom {}))
(defonce audio-uris (atom {}))
(defonce progress-timer (atom nil))
(defonce current-player-key (reagent/atom nil))

(defn get-player-key
  [message-id in-pinned-view?]
  (str in-pinned-view? message-id))

(defn destroy-player
  [player-key]
  (when-let [player (@active-players player-key)]
    (audio/destroy-player player)
    (swap! active-players dissoc player-key)))

(defn update-state
  [state new-state]
  (when-not (= @state new-state)
    (reset! state new-state)))

(defn seek-player
  [player-key player-state value on-success]
  (when-let [player (@active-players player-key)]
    (audio/seek-player
     player
     value
     #(when on-success (on-success))
     #(update-state player-state :error))
    (update-state player-state :seeking)))

(defn download-audio-http
  [base64-uri on-success]
  (-> (.config ReactNativeBlobUtil (clj->js {:trusty platform/ios?}))
      (.fetch "GET" (str base64-uri))
      (.then #(on-success (.base64 ^js %)))
      (.catch #(log/error "could not fetch audio " base64-uri))))

(defn create-player
  [{:keys [progress player-state player-key]} audio-url on-success]
  (download-audio-http
   audio-url
   (fn [base64-data]
     (let [player (audio/new-player
                   (str "data:audio/aac;base64," base64-data)
                   {:autoDestroy                 false
                    :continuesToPlayInBackground false}
                   (fn []
                     (update-state player-state :ready-to-play)
                     (reset! progress 0)
                     (when (and @progress-timer (= @current-player-key player-key))
                       (js/clearInterval @progress-timer)
                       (reset! progress-timer nil))))]
       (audio/set-player-wake-lock player true)
       (swap! active-players assoc player-key player)
       (audio/prepare-player
        player
        #(when on-success (on-success))
        #(update-state player-state :error)))))
  (update-state player-state :preparing))

(defn play-pause-player
  [{:keys [player-key player-state progress message-id audio-duration-ms seeking-audio?
           user-interaction? mediaserver-port]
    :as   params}]
  (let [audio-uri (str media-server-uri-prefix
                       mediaserver-port
                       audio-path
                       uri-param
                       message-id)
        player    (@active-players player-key)
        playing?  (= @player-state :playing)]
    (when-not playing?
      (reset! current-player-key player-key))
    (if (and player
             (= (@audio-uris player-key) audio-uri)
             (not= (audio/get-state player) audio/IDLE))
      (audio/toggle-playpause-player
       player
       (fn []
         (update-state player-state :playing)
         (when @progress-timer
           (js/clearInterval @progress-timer))
         (reset! progress-timer
           (js/setInterval
            (fn []
              (let [player       (@active-players player-key)
                    current-time (audio/get-player-current-time player)
                    playing?     (= @player-state :playing)]
                (when (and playing? (not @seeking-audio?) (> current-time 0))
                  (reset! progress current-time))))
            100)))
       (fn []
         (update-state player-state :ready-to-play)
         (when (and @progress-timer user-interaction?)
           (js/clearInterval @progress-timer)
           (reset! progress-timer nil)))
       #(update-state player-state :error))
      (do
        (swap! audio-uris assoc player-key audio-uri)
        (destroy-player player-key)
        (create-player params
                       audio-uri
                       (fn []
                         (reset! seeking-audio? false)
                         (if (> @progress 0)
                           (let [seek-time         (* audio-duration-ms @progress)
                                 checked-seek-time (if (<= @progress 1) seek-time @progress)]
                             (seek-player
                              player-key
                              player-state
                              checked-seek-time
                              #(play-pause-player params)))
                           (play-pause-player params))))))))

(defn f-audio-message
  [player-state progress seeking-audio? {:keys [audio-duration-ms message-id]}
   {:keys [in-pinned-view?]}]
  (let [player-key       (get-player-key message-id in-pinned-view?)
        player           (@active-players player-key)
        duration         (if (and player (not (#{:preparing :not-loaded :error} @player-state)))
                           (audio/get-player-duration player)
                           audio-duration-ms)
        time-secs        (quot
                          (if (or @seeking-audio? (#{:playing :seeking} @player-state))
                            (if (<= @progress 1) (* duration @progress) @progress)
                            duration)
                          1000)
        paused?          (= (audio/get-state player) audio/PAUSED)
        app-state        (rf/sub [:app-state])
        mediaserver-port (rf/sub [:mediaserver/port])]
    (rn/use-effect (fn [] #(destroy-player player-key)))
    (rn/use-effect
     (fn []
       (when (or
              (and (some? @current-player-key)
                   (not= @current-player-key player-key)
                   (= @player-state :playing))
              (and platform/ios?
                   (= @current-player-key player-key)
                   (not= app-state "active")
                   (= @player-state :playing)))
         (play-pause-player {:player-key        player-key
                             :player-state      player-state
                             :progress          progress
                             :message-id        message-id
                             :audio-duration-ms duration
                             :seeking-audio?    seeking-audio?
                             :user-interaction? false
                             :mediaserver-port  mediaserver-port})))
     [@current-player-key app-state mediaserver-port])
    (if (= @player-state :error)
      [quo/text
       {:style               style/error-label
        :accessibility-label :audio-error-label
        :weight              :medium
        :size                :paragraph-2}
       (i18n/label :error-loading-audio)]
      [rn/view
       {:accessibility-label :audio-message-container
        :style               (style/container)}
       [rn/touchable-opacity
        {:accessibility-label :play-pause-audio-message-button
         :on-press            #(play-pause-player {:player-key        player-key
                                                   :player-state      player-state
                                                   :progress          progress
                                                   :message-id        message-id
                                                   :audio-duration-ms duration
                                                   :seeking-audio?    seeking-audio?
                                                   :user-interaction? true
                                                   :mediaserver-port  mediaserver-port})
         :style               (style/play-pause-container)}
        [quo/icon
         (cond
           (= @player-state :preparing)
           :i/loading
           (and (= @player-state :playing) (not paused?))
           :i/pause-audio
           :else :i/play-audio)
         {:size  20
          :color colors/white}]]
       [:f> quo/soundtrack
        {:style                 style/slider-container
         :audio-current-time-ms progress
         :player-ref            (@active-players player-key)
         :seeking-audio?        seeking-audio?}]
       [quo/text
        {:style               style/timestamp
         :accessibility-label :audio-duration-label
         :weight              :medium
         :size                :paragraph-2}
        (gstring/format "%02d:%02d" (quot time-secs 60) (mod time-secs 60))]])))

(defn audio-message
  [message context]
  (let [player-state   (reagent/atom :not-loaded)
        progress       (reagent/atom 0)
        seeking-audio? (reagent/atom false)]
    (fn []
      [:f> f-audio-message player-state progress seeking-audio? message context])))
