(ns quo.components.messages.system-message.style
  (:require
    [quo.foundations.colors :as colors]))

(defn text-color
  [theme]
  (colors/theme-colors colors/neutral-100 colors/white theme))

(defn time-color
  [theme]
  (colors/theme-colors colors/neutral-40 colors/neutral-50 theme))

(def sm-icon-wrapper {:margin-right 8})

(def sm-timestamp-wrapper {:margin-left 8})

(defn sm-timestamp-text
  [theme]
  {:color          (time-color theme)
   :text-transform :none})

(def sm-user-avatar-wrapper {:margin-right 4})

(def split-text-wrapper {:flex-direction :row :flex-shrink 0 :align-items :center})

(defn each-split-text
  [theme indx label-vector]
  {:color        (text-color theme)
   :margin-right (if (= indx (dec (count label-vector)))
                   0
                   3)})

(def system-message-base-wrapper
  {:flex-direction :row
   :flex           1})

(def system-message-base-content-wrapper
  {:align-self     :center
   :flex-direction :row
   :flex           1})

(def system-message-deleted-wrapper
  {:flex-direction :row
   :align-items    :center})

(defn system-message-deleted-text
  [theme]
  {:color (text-color theme)})

(def system-message-contact-wrapper
  {:flex-direction :row
   :align-items    :center
   :flex-shrink    1
   :flex-wrap      :nowrap})

(def system-message-contact-account-wrapper
  {:flex-direction :row
   :align-items    :center
   :flex-shrink    1})

(def system-message-contact-account-name
  {:flex-shrink 1})

(def system-message-contact-request-wrapper
  {:flex-direction :row
   :align-items    :center
   :flex-shrink    1
   :flex-wrap      :nowrap})

(def system-message-contact-request-account-wrapper
  {:flex-direction :row
   :align-items    :center
   :margin-left    4
   :flex-shrink    1})

(def system-message-contact-request-account-name
  {:flex-shrink 1})

(def system-message-pinned-wrapper
  {:flex 1})

(def system-message-pinned-content-wrapper
  {:flex-direction :row
   :align-items    :center
   :flex-wrap      :nowrap})

(def system-message-pinned-content-pinned-by
  {:flex-shrink 1})

(def system-message-deleted-animation-start-bg-color colors/danger-50-opa-5)
(def system-message-deleted-animation-end-bg-color colors/danger-50-opa-0)

(def system-message-wrapper
  {:padding-horizontal 12
   :padding-vertical   8
   :flex               1})
