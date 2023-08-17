(ns status-im2.contexts.quo-preview.tags.context-tags
  (:require [quo2.components.tags.context-tag.view :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def group-avatar-default-params
  {:size  :small
   :color :purple})

(def example-pk
  "0x04fcf40c526b09ff9fb22f4a5dbd08490ef9b64af700870f8a0ba2133f4251d5607ed83cd9047b8c2796576bc83fa0de23a13a4dced07654b8ff137fe744047917")
(def example-pk2
  "0x04c178513eb741e8c4e50326b22baefa7d60a2f4eb81e328c4bbe0b441f87b2a014a5907a419f5897fc3c0493a0ff9db689a1999d6ca7fdc63119dd1981d0c7ccf")
(def example-photo
  "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAIAQMAAAD+wSzIAAAABlBMVEX///+/v7+jQ3Y5AAAADklEQVQI12P4AIX8EAgALgAD/aNpbtEAAAAASUVORK5CYII")
(def example-photo2
  "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAADgdz34AAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAAApgAAAKYB3X3/OAAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAANCSURBVEiJtZZPbBtFFMZ/M7ubXdtdb1xSFyeilBapySVU8h8OoFaooFSqiihIVIpQBKci6KEg9Q6H9kovIHoCIVQJJCKE1ENFjnAgcaSGC6rEnxBwA04Tx43t2FnvDAfjkNibxgHxnWb2e/u992bee7tCa00YFsffekFY+nUzFtjW0LrvjRXrCDIAaPLlW0nHL0SsZtVoaF98mLrx3pdhOqLtYPHChahZcYYO7KvPFxvRl5XPp1sN3adWiD1ZAqD6XYK1b/dvE5IWryTt2udLFedwc1+9kLp+vbbpoDh+6TklxBeAi9TL0taeWpdmZzQDry0AcO+jQ12RyohqqoYoo8RDwJrU+qXkjWtfi8Xxt58BdQuwQs9qC/afLwCw8tnQbqYAPsgxE1S6F3EAIXux2oQFKm0ihMsOF71dHYx+f3NND68ghCu1YIoePPQN1pGRABkJ6Bus96CutRZMydTl+TvuiRW1m3n0eDl0vRPcEysqdXn+jsQPsrHMquGeXEaY4Yk4wxWcY5V/9scqOMOVUFthatyTy8QyqwZ+kDURKoMWxNKr2EeqVKcTNOajqKoBgOE28U4tdQl5p5bwCw7BWquaZSzAPlwjlithJtp3pTImSqQRrb2Z8PHGigD4RZuNX6JYj6wj7O4TFLbCO/Mn/m8R+h6rYSUb3ekokRY6f/YukArN979jcW+V/S8g0eT/N3VN3kTqWbQ428m9/8k0P/1aIhF36PccEl6EhOcAUCrXKZXXWS3XKd2vc/TRBG9O5ELC17MmWubD2nKhUKZa26Ba2+D3P+4/MNCFwg59oWVeYhkzgN/JDR8deKBoD7Y+ljEjGZ0sosXVTvbc6RHirr2reNy1OXd6pJsQ+gqjk8VWFYmHrwBzW/n+uMPFiRwHB2I7ih8ciHFxIkd/3Omk5tCDV1t+2nNu5sxxpDFNx+huNhVT3/zMDz8usXC3ddaHBj1GHj/As08fwTS7Kt1HBTmyN29vdwAw+/wbwLVOJ3uAD1wi/dUH7Qei66PfyuRj4Ik9is+hglfbkbfR3cnZm7chlUWLdwmprtCohX4HUtlOcQjLYCu+fzGJH2QRKvP3UNz8bWk1qMxjGTOMThZ3kvgLI5AzFfo379UAAAAASUVORK5CYII=")

(def coinbase-community-image (resources/get-mock-image :coinbase))

(def main-descriptor
  [{:label   "Type"
    :key     :type
    :type    :select
    :options [{:key   :public-key
               :value "Public key"}
              {:key   :avatar
               :value "Avatar"}
              {:key   :group-avatar
               :value "Group avatar"}
              {:key   :context-tag
               :value "Context tag"}
              {:key   :audio
               :value "Audio"}
              {:key   :community
               :value "Community"}]}
   {:label "Blur"
    :key   :blur?
    :type  :boolean}])

(def context-tag-descriptor
  [{:label "Show avatar"
    :key   :show-avatar?
    :type  :boolean}
   {:label "Label"
    :key   :label
    :type  :text}
   {:label "Channel name"
    :key   :channel-name
    :type  :text}
   {:label "Avatar placeholder"
    :key   :no-avatar-placeholder?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:label                  "Name"
                             :channel-name           "Channel"
                             :type                   :group-avatar
                             :duration               "00:32"
                             :show-avatar?           true
                             :no-avatar-placeholder? false})]
    (fn []
      (let [contacts             {example-pk  {:public-key   example-pk
                                               :primary-name "Automatic incompatible Coati"
                                               :photo        example-photo}
                                  example-pk2 {:public-key   example-pk2
                                               :primary-name "Clearcut Flickering Rattlesnake"
                                               :photo        example-photo2}}
            contacts-public-keys (map (fn [{:keys [public-key]}]
                                        {:key   public-key
                                         :value (get-in contacts [public-key :primary-name])})
                                      (vals contacts))
            current-username     (if (seq (:contact @state))
                                   (->> @state
                                        :contact
                                        contacts
                                        multiaccounts/displayed-name)
                                   "Please select a user")
            descriptor           (cond
                                   (= (:type @state) :context-tag)
                                   (into main-descriptor context-tag-descriptor)

                                   (= (:type @state) :group-avatar)
                                   (conj main-descriptor
                                         {:label "Label"
                                          :key   :label
                                          :type  :text})

                                   (= (:type @state) :avatar)
                                   (let [photo (-> @state :contact contacts :photo)]
                                     (when-not (contains? @state :contacts)
                                       (swap! state assoc :contacts contacts-public-keys))
                                     (when-not (= (:photo @state)
                                                  photo)
                                       (swap! state assoc :photo photo))
                                     (conj main-descriptor
                                           {:label   "Contacts"
                                            :key     :contact
                                            :type    :select
                                            :options contacts-public-keys}))

                                   (= (:type @state) :community)
                                   (conj main-descriptor
                                         {:label "Community name"
                                          :key   :label
                                          :type  :text})

                                   (= (:type @state) :audio)
                                   (conj main-descriptor
                                         {:label "Duration"
                                          :key   :duration
                                          :type  :text})

                                   :else
                                   main-descriptor)]
        [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
         [rn/view {:style {:padding-bottom 150}}
          [rn/view {:style {:flex 1}}
           [preview/customizer state descriptor]]
          [rn/view {:style {:padding-vertical 60}}
           [preview/blur-view
            {:style                 {:flex              1
                                     :margin-vertical   20
                                     :margin-horizontal 40}
             :show-blur-background? (:blur? @state)}
            (case (:type @state)
              :context-tag  [quo2/context-tag
                             {:blur?                  (:blur? @state)
                              :size                   :small
                              :color                  :purple
                              :no-avatar-placeholder? (:no-avatar-placeholder? @state)}
                             (when (:show-avatar? @state)
                               example-photo2)
                             (:label @state)
                             (:channel-name @state)]

              :group-avatar [quo2/group-avatar-tag (:label @state)
                             {:blur?               (:blur? @state)
                              :size                20
                              :customization-color :purple}]

              :public-key   [quo2/public-key-tag
                             {:blur? (:blur? @state)
                              :size  :small
                              :color :purple}
                             example-pk]

              :avatar       [quo2/user-avatar-tag
                             {:blur? (:blur? @state)
                              :size  :small
                              :color :purple}
                             current-username (:photo @state)]

              :audio        [quo2/audio-tag (:duration @state) {:blur? (:blur? @state)}]

              :community    [quo2/community-tag
                             coinbase-community-image
                             (:label @state)
                             {:blur? (:blur? @state)}])]]]]))))

(defn preview-context-tags
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/white
                                                   colors/neutral-95)
            :flex             1}}
   [rn/flat-list
    {:style                        {:flex 1}
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
