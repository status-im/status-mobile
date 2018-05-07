(ns status-im.chat.views.photos
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.chat.styles.photos :as style]
            [status-im.utils.identicon :as identicon]
            [clojure.string :as string]
            [status-im.react-native.resources :as resources]))

(defn- photo [from photo-path]
  [react/view
   [react/image {:source (if (and (not (string/blank? photo-path))
                                  (string/starts-with? photo-path "contacts://"))
                           (->> (string/replace photo-path #"contacts://" "")
                                (keyword)
                                (get resources/contacts))
                           {:uri photo-path})
                 :style  style/photo}]])

(defview member-photo [from]
  (letsubs [photo-path [:get-photo-path from]]
           (photo from (if (string/blank? photo-path)
                         (identicon/identicon from)
                         photo-path))))
