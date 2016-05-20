(ns status-im.contacts.views.contact-inner
  (:require [clojure.string :as s]
            [status-im.components.react :refer [view image text]]
            [status-im.resources :as res]
            [status-im.contacts.styles :as st]))

(defn contact-photo [{:keys [photo-path]}]
  [view st/contact-photo-container
   [image {:source (if (s/blank? photo-path)
                     res/user-no-photo
                     {:uri photo-path})
           :style  st/photo-image}]])

(defn contact-online [{:keys [online]}]
  (when online
    [view st/online-container
     [view st/online-dot-left]
     [view st/online-dot-right]]))

(defn contact-inner-view [{:keys [name photo-path online]}]
  [view st/contact-container
   [view st/photo-container
    [contact-photo {:photo-path photo-path}]
    [contact-online {:online online}]]
   [view st/name-container
    [text {:style st/name-text}
     (if (pos? (count name))
       name
       ;; todo is this correct behaviour?
       "Noname")]]])
