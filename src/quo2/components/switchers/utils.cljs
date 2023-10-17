(ns quo2.components.switchers.utils
  (:require
    [utils.i18n :as i18n]))

(defn subtitle
  [type {:keys [photos]}]
  (case type
    :message
    (i18n/label :t/message)

    :photo
    (i18n/label
     (if (= (count photos) 1)
       :t/one-photo
       :t/n-photos)
     {:count (count photos)})

    :sticker
    (i18n/label :t/sticker)

    :gif
    (i18n/label :t/gif)

    :audio
    (i18n/label :t/audio-message)

    :community
    (i18n/label :t/link-to-community)

    :link
    (i18n/label :t/external-link)

    :code
    (i18n/label :t/code-snippet)

    ""))
