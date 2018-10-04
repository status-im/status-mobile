(ns status-im.ui.screens.chat.utils
  (:require [status-im.utils.gfycat.core :as gfycat]
            [status-im.i18n :as i18n]))

(defn format-author [from username]
  (str (when username (str username " :: "))
       (gfycat/generate-gfy from))) ; TODO: We defensively generate the name for now, to be revisited when new protocol is defined

(defn format-reply-author [from username current-public-key]
  (or (and (= from current-public-key) (i18n/label :t/You))
      (format-author from username)))

