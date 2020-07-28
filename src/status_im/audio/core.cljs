(ns status-im.audio.core
  (:require ["@react-native-community/audio-toolkit" :refer (Player Recorder MediaStates)]))

;; get mediastates from react module
(def PLAYING (.-PLAYING ^js MediaStates))
(def PAUSED (.-PAUSED ^js MediaStates))
(def RECORDING (.-RECORDING ^js MediaStates))
(def PREPARED (.-PREPARED ^js MediaStates))
(def IDLE (.-IDLE ^js MediaStates))
(def ERROR (.-ERROR ^js MediaStates))
(def DESTROYED (.-DESTROYED ^js MediaStates))
(def SEEKING (.-SEEKING ^js MediaStates))

(def default-recorder-options {:filename "recording.aac"
                               :bitrate 32000
                               :channels 1
                               :sampleRate 22050
                               :quality "medium" ; ios only
                               :meteringInterval 50})

(defn get-state [player-recorder]
  (when player-recorder
    (.-state ^js player-recorder)))

(defn new-recorder [options on-meter on-ended]
  (let [recorder (new ^js Recorder
                      (:filename options)
                      (clj->js options))]
    (when on-meter
      (.on ^js recorder "meter" on-meter))
    (when on-ended
      (.on ^js recorder "ended" on-ended))))

(defn new-player [audio options on-ended]
  (let [player (new ^js Player
                    audio
                    (clj->js options))]
    (when on-ended
      (.on ^js player "ended" on-ended))))

(defn prepare-player [player on-prepared on-error]
  (when (and player (.-canPrepare ^js player))
    (.prepare ^js player #(if %
                            (on-error {:error (.-err %) :message (.-message %)})
                            (on-prepared)))))

(defn prepare-recorder [recorder on-prepared on-error]
  (when (and recorder (.-canPrepare ^js recorder))
    (.prepare ^js recorder (fn [err _]
                             (if err
                               (on-error {:error (.-err err) :message (.-message err)})
                               (on-prepared))))))

(defn start-recording [recorder on-start on-error]
  (when (and recorder
             (or
              (.-canRecord ^js recorder)
              (.-canPrepare ^js recorder)))
    (.record ^js recorder #(if %
                             (on-error {:error (.-err %) :message (.-message %)})
                             (on-start)))))

(defn stop-recording [recorder on-stop on-error]
  (if (and recorder (#{RECORDING PAUSED} (get-state recorder)))
    (.stop ^js recorder #(if %
                           (on-error {:error (.-err %) :message (.-message %)})
                           (on-stop)))
    (on-stop)))

(defn pause-recording [recorder on-pause on-error]
  (when (and recorder (.-isRecording ^js recorder))
    (.pause ^js recorder #(if %
                            (on-error {:error (.-err %) :message (.-message %)})
                            (on-pause)))))

(defn start-playing [player on-start on-error]
  (when (and player (.-canPlay ^js player))
    (.play ^js player #(if %
                         (on-error {:error (.-err %) :message (.-message %)})
                         (on-start)))))

(defn stop-playing [player on-stop on-error]
  (if (and player (.-isPlaying ^js player))
    (.stop ^js player #(if %
                         (on-error {:error (.-err %) :message (.-message %)})
                         (on-stop)))
    (on-stop)))

(defn get-recorder-file-path [recorder]
  (when recorder
    (.-fsPath ^js recorder)))

(defn get-player-duration [player]
  (when (and player (.-canPlay ^js player))
    (.-duration ^js player)))

(defn get-player-current-time [player]
  (when (and player (.-canPlay ^js player))
    (.-currentTime ^js player)))

(defn toggle-playpause-player [player on-play on-pause on-error]
  (when (and player (.-canPlay ^js player))
    (.playPause ^js player (fn [error pause?]
                             (if error
                               (on-error {:error (.-err error) :message (.-message error)})
                               (if pause?
                                 (on-pause)
                                 (on-play)))))))

(defn seek-player [player value on-seek on-error]
  (when (and player (.-canPlay ^js player))
    (.seek ^js player value #(if %
                               (on-error {:error (.-err %) :message (.-message %)})
                               (on-seek)))))

(defn can-play? [player]
  (and player (.-canPlay ^js player)))

(defn destroy-recorder [recorder]
  (stop-recording recorder
                  #(when (and recorder (not= (get-state recorder) DESTROYED))
                     (.destroy ^js recorder))
                  #()))

(defn destroy-player [player]
  (stop-playing player
                #(when (and player (not= (get-state player) IDLE))
                   (.destroy ^js player))
                #()))