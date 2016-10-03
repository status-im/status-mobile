(ns status-im.components.tabs.tab
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                animated-view
                                                text
                                                image
                                                touchable-highlight]]
            [reagent.core :as r]
            [status-im.components.tabs.styles :as st]
            [status-im.components.animation :as anim]))

(defn animation-logic [val]
  (fn []
    (anim/start (anim/spring val {:toValue 0.1
                                  :tension 30}))))

(defview tab [_]
  (let [icon-anim-value (anim/create-value 10)
        text-anim-value (anim/create-value 20)
        on-update       (comp (animation-logic icon-anim-value)
                              (animation-logic text-anim-value))]
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :component-will-receive-props
       (fn []
         (anim/set-value icon-anim-value 5)
         (anim/set-value text-anim-value 20))
       :reagent-render
       (fn [{:keys [view-id title icon selected-view-id]}]
         [touchable-highlight {:style    st/tab
                               :disabled (= view-id selected-view-id)
                               :onPress  #(dispatch [:navigate-to-tab view-id])}
          [view {:style st/tab-container}
           [animated-view {:style (st/animated-offset (if (= selected-view-id view-id)
                                                        icon-anim-value
                                                        0))}
            [image {:source {:uri icon}
                    :style  st/tab-icon}]]
           (when (= selected-view-id view-id)
             [animated-view {:style (st/animated-offset text-anim-value)}
              [text {:style st/tab-title} title]])]])})))
