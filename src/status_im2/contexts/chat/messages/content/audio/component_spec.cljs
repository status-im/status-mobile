(ns status-im2.contexts.chat.messages.content.audio.component-spec
  (:require [status-im2.contexts.chat.messages.content.audio.view :as audio-message]
            [test-helpers.component :as h]
            [react-native.audio-toolkit :as audio]
            [re-frame.core :as re-frame]))

(def message
  {:audio-duration-ms 5000
   :message-id        "message-id"})

(def context
  {:in-pinned-view? false})

(defn setup-subs
  [subscriptions]
  (doseq [keyval subscriptions]
    (re-frame/reg-sub
     (key keyval)
     (fn [_] (val keyval)))))

(h/describe "audio message"
  (h/before-each
   #(setup-subs {:mediaserver/port 1000
                 :app-state        "active"}))

  (h/test "renders correctly"
    (h/render [audio-message/audio-message message context])
    (h/is-truthy (h/get-by-label-text :audio-message-container)))

  (h/test "press play calls audio/toggle-playpause-player"
    (with-redefs [audio/toggle-playpause-player     (js/jest.fn)
                  audio/new-player                  (fn [_ _ _] {})
                  audio/destroy-player              #()
                  audio/prepare-player              (fn [_ on-success _] (on-success))
                  audio-message/download-audio-http (fn [_ on-success] (on-success "audio-uri"))]
      (h/render [audio-message/audio-message message context])
      (h/fire-event
       :on-press
       (h/get-by-label-text :play-pause-audio-message-button))
      (-> (h/expect audio/toggle-playpause-player)
          (.toHaveBeenCalledTimes 1))))

  (h/test "press play renders error"
    (h/render [audio-message/audio-message message context])
    (with-redefs [audio/toggle-playpause-player     (fn [_ _ _ on-error] (on-error))
                  audio/new-player                  (fn [_ _ _] {})
                  audio/destroy-player              #()
                  audio/prepare-player              (fn [_ on-success _] (on-success))
                  audio-message/download-audio-http (fn [_ on-success] (on-success "audio-uri"))]
      (h/fire-event
       :on-press
       (h/get-by-label-text :play-pause-audio-message-button))
      (h/wait-for #(h/is-truthy (h/get-by-label-text :audio-error-label))))))
