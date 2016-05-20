(ns syng-im.group-settings.views.member
  (:require [clojure.string :as s]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view
                                              image
                                              text
                                              icon
                                              touchable-highlight]]
            [syng-im.resources :as res]
            [syng-im.group-settings.styles.member :as st]))

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

(defn member-view [{:keys [whisper-identity name photo-path online role]}]
  [view st/contact-container
   [view st/photo-container
    [contact-photo {:photo-path photo-path}]
    [contact-online {:online online}]]
   [view st/info-container
    [text {:style st/name-text}
     (if (pos? (count name))
       name
       ;; todo is this correct behaviour?
       "Noname")]
    ;; TODO implement :role property for group chat contact
    (when role
      [text {:style st/role-text}
       role])]
   [touchable-highlight
    {:on-press #(dispatch [:set :group-settings-selected-member whisper-identity])}
    [view st/more-btn
     [icon :more-vertical st/more-btn-icon]]]])
