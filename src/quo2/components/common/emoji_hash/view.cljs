(ns quo2.components.common.emoji-hash.view
  (:require [quo2.components.common.emoji-hash.style :as style]
            [quo2.foundations.twemoji :as twemoji]
            [quo2.theme :as quo.theme]
            [react-native.core :as rn]))

(defn- view-internal
  "Opts:
    :emoji-hash - vector consist of emojis -> [😄 😂 🫣 🍑 😇 🤢 😻 🥷🏻 🦸🏻‍♀️ 🌶️ 💃 🚌 🧑🏻‍🎄 🪗]

    :container-style - map (optional)

    :theme - keyword -> :light/:dark"
  [{:keys [emoji-hash container-style]}]
  (into [rn/view {:style (merge style/root-container container-style)}]
        (map #(with-meta [rn/view {:style style/emoji-container}
                          [twemoji/twemoji
                           {:image-style {:height 15
                                          :width  15}} %]]
                         {:key %})
             emoji-hash)))

(def view (quo.theme/with-theme view-internal))
