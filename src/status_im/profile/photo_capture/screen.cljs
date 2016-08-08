(ns status-im.profile.photo-capture.screen
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [clojure.walk :refer [keywordize-keys]]
            [status-im.components.react :refer [view
                                                image
                                                touchable-highlight]]
            [status-im.components.camera :refer [camera
                                                 aspects
                                                 capture-targets]]
            [status-im.components.styles :refer [toolbar-background1
                                                 icon-search
                                                 icon-back]]
            [status-im.components.icons.custom-icons :refer [ion-icon]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.utils.image-processing :refer [img->base64]]
            [status-im.profile.photo-capture.styles :as st]
            [status-im.i18n :refer [label]]
            [reagent.core :as r]))

(defn image-captured [path]
  (let [path       (subs path 5)
        on-success (fn [base64]
                     (dispatch [:set-in [:profile-edit :photo-path] (str "data:image/jpeg;base64," base64)])
                     (dispatch [:navigate-back]))
        on-error   (fn [type error]
                     (.log js/console type error))]
    (img->base64 path on-success on-error)))

(defn profile-photo-capture [{platform-specific :platform-specific}]
  (let [camera-ref (r/atom nil)]
    [view st/container
     [status-bar {:platform-specific platform-specific}]
     [toolbar {:title            (label :t/image-source-title)
               :nav-action       {:image   {:source {:uri :icon_back}
                                            :style  icon-back}
                                  :handler #(dispatch [:navigate-back])}
               :background-color toolbar-background1}]
     [camera {:style         {:flex 1}
              :aspect        (:fill aspects)
              :captureTarget (:disk capture-targets)
              :type          "front"
              :ref           #(reset! camera-ref %)}]
     [view {:style {:padding          10
                    :background-color toolbar-background1}}
      [touchable-highlight {:style    {:align-self "center"}
                            :on-press (fn []
                                        (let [camera @camera-ref]
                                          (-> (.capture camera)
                                              (.then image-captured))))}
       [view
        [ion-icon {:name  :md-camera
                   :style {:font-size 36}}]]]]]))