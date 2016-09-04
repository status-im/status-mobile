(ns status-im.profile.handlers
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.components.react :refer [show-image-picker]]
            [status-im.utils.image-processing :refer [img->base64]]
            [status-im.i18n :refer [label]]
            [status-im.utils.handlers :as u :refer [get-hashtags]]
            [clojure.string :as str]))

(defn message-user [identity]
  (when identity
    (dispatch [:navigate-to :chat identity])))

(defn update-profile [{name       :name
                       email      :email
                       photo-path :photo-path
                       status     :status}
                      {new-name       :name
                       new-email      :email
                       new-status     :status
                       new-photo-path :photo-path}]
  (let [new-name        (if (or (not new-name) (str/blank? new-name)) name new-name)
        status-updated? (and (not= new-status nil)
                             (not= status new-status))]
    (when status-updated?
      (let [hashtags (get-hashtags new-status)]
        (when-not (empty? hashtags)
          (dispatch [:broadcast-status new-status hashtags]))))
    (dispatch [:account-update {:name       new-name
                                :email      (or new-email email)
                                :status     (or new-status status)
                                :photo-path (or new-photo-path photo-path)}])))

(register-handler :open-image-picker
   (u/side-effect!
     (fn [_ _]
       (show-image-picker
         (fn [image]
           (let [path       (-> (js->clj image)
                                (get "path")
                                (subs 7))
                 on-success (fn [base64]
                              (dispatch [:set-in [:profile-edit :photo-path] (str "data:image/jpeg;base64," base64)]))
                 on-error   (fn [type error]
                              (.log js/console type error))]
             (img->base64 path on-success on-error)))))))

(register-handler :open-image-source-selector
  (u/side-effect!
    (fn [_ [_ list-selection-fn]]
      (list-selection-fn {:title       (label :t/image-source-title)
                          :options     [(label :t/image-source-make-photo) (label :t/image-source-gallery)]
                          :callback    (fn [index]
                                         (case index
                                           0 (dispatch [:show-profile-photo-capture])
                                           1 (dispatch [:open-image-picker])
                                           :default))
                          :cancel-text (label :t/image-source-cancel)}))))
