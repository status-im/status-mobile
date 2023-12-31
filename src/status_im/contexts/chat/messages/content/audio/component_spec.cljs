(ns status-im.contexts.chat.messages.content.audio.component-spec
  (:require
    [react-native.audio-toolkit :as audio]
    [status-im.contexts.chat.messages.content.audio.view :as audio-message]
    [test-helpers.component :as h]))

;; We can't rely on `with-redefs` with async code.
(set! audio/destroy-player #())

(def message
  {:audio-duration-ms 5000
   :message-id        "message-id"})

(def context
  {:in-pinned-view? false})

(h/describe "audio message"
  (h/setup-restorable-re-frame)

  (h/before-each
   (fn []
     (h/setup-subs {:mediaserver/port 1000
                    :app-state        "active"})))

  (h/test "renders correctly"
    (h/render [audio-message/audio-message message context])
    (h/is-truthy (h/get-by-label-text :audio-message-container)))

  (h/test "press play calls audio/toggle-playpause-player"
    (with-redefs [audio/toggle-playpause-player     (js/jest.fn)
                  audio/new-player                  (fn [_ _ _] {})
                  audio/prepare-player              (fn [_ on-success _] (on-success))
                  audio-message/download-audio-http (fn [_ on-success] (on-success "audio-uri"))]
      (h/render [audio-message/audio-message message context])
      (h/fire-event :on-press (h/get-by-label-text :play-pause-audio-message-button))
      (-> (h/expect audio/toggle-playpause-player)
          (.toHaveBeenCalledTimes 1))))

  (h/test "press play renders error"
    (with-redefs [audio/new-player                  (fn [_ _ _] {})
                  audio/prepare-player              (fn [_ on-success _] (on-success))
                  audio-message/download-audio-http (fn [_ on-success] (on-success "audio-uri"))]
      (h/render [audio-message/audio-message message context])
      (h/fire-event :on-press (h/get-by-label-text :play-pause-audio-message-button)))
    (with-redefs [audio/toggle-playpause-player (fn [_ _ _ on-error] (on-error))]
      (h/fire-event :on-press (h/get-by-label-text :play-pause-audio-message-button))
      (h/wait-for #(h/get-by-label-text :audio-error-label)))))
