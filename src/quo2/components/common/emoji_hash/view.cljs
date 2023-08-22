(ns quo2.components.common.emoji-hash.view
  (:require [quo2.components.common.emoji-hash.style :as style]
            [quo2.foundations.twemoji :as twemoji]
            [quo2.theme :as quo.theme]
            [react-native.core :as rn]))

(defn- view-internal
  "Opts:
    :emoji-hash - vector consist of emojis -> [😄 😂 🫣 🍑 😇 🤢 😻 🥷🏻 🦸🏻‍♀️ 🌶️ 💃 🚌 🧑🏻‍🎄 🪗]

    :size - number (optional) - 16 [default] 

    :container-style - map (optional)

    :emoji-container-style - map (optional)

    :theme - keyword -> :light/:dark"
  [{:keys [emoji-hash size container-style emoji-container-style]}]
  (let [emoji-size (or size 16)]
    (into [rn/view {:style (merge style/root-container container-style)}]
          (map #(with-meta [rn/view {:style (merge style/emoji-container emoji-container-style)}
                            [twemoji/twemoji
                             {:image-style {:height emoji-size
                                            :width  emoji-size}} %]]
                           {:key %})
               emoji-hash))))

(def view (quo.theme/with-theme view-internal))
