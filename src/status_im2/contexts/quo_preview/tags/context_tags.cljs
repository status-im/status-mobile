(ns status-im2.contexts.quo-preview.tags.context-tags
  (:require [reagent.core :as reagent]
            [status-im.multiaccounts.core :as multiaccounts]
            [react-native.core :as rn]
            [status-im2.contexts.quo-preview.preview :as preview]
            [quo2.foundations.colors :as colors]
            [quo2.components.tags.context-tags :as quo2]))

(def group-avatar-default-params
  {:size :small
   :color :purple})

(def example-pk "0x04fcf40c526b09ff9fb22f4a5dbd08490ef9b64af700870f8a0ba2133f4251d5607ed83cd9047b8c2796576bc83fa0de23a13a4dced07654b8ff137fe744047917")
(def example-pk2 "0x04c178513eb741e8c4e50326b22baefa7d60a2f4eb81e328c4bbe0b441f87b2a014a5907a419f5897fc3c0493a0ff9db689a1999d6ca7fdc63119dd1981d0c7ccf")
(def example-photo "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAIAQMAAAD+wSzIAAAABlBMVEX///+/v7+jQ3Y5AAAADklEQVQI12P4AIX8EAgALgAD/aNpbtEAAAAASUVORK5CYII")
(def example-photo2 "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAADgdz34AAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAAApgAAAKYB3X3/OAAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAANCSURBVEiJtZZPbBtFFMZ/M7ubXdtdb1xSFyeilBapySVU8h8OoFaooFSqiihIVIpQBKci6KEg9Q6H9kovIHoCIVQJJCKE1ENFjnAgcaSGC6rEnxBwA04Tx43t2FnvDAfjkNibxgHxnWb2e/u992bee7tCa00YFsffekFY+nUzFtjW0LrvjRXrCDIAaPLlW0nHL0SsZtVoaF98mLrx3pdhOqLtYPHChahZcYYO7KvPFxvRl5XPp1sN3adWiD1ZAqD6XYK1b/dvE5IWryTt2udLFedwc1+9kLp+vbbpoDh+6TklxBeAi9TL0taeWpdmZzQDry0AcO+jQ12RyohqqoYoo8RDwJrU+qXkjWtfi8Xxt58BdQuwQs9qC/afLwCw8tnQbqYAPsgxE1S6F3EAIXux2oQFKm0ihMsOF71dHYx+f3NND68ghCu1YIoePPQN1pGRABkJ6Bus96CutRZMydTl+TvuiRW1m3n0eDl0vRPcEysqdXn+jsQPsrHMquGeXEaY4Yk4wxWcY5V/9scqOMOVUFthatyTy8QyqwZ+kDURKoMWxNKr2EeqVKcTNOajqKoBgOE28U4tdQl5p5bwCw7BWquaZSzAPlwjlithJtp3pTImSqQRrb2Z8PHGigD4RZuNX6JYj6wj7O4TFLbCO/Mn/m8R+h6rYSUb3ekokRY6f/YukArN979jcW+V/S8g0eT/N3VN3kTqWbQ428m9/8k0P/1aIhF36PccEl6EhOcAUCrXKZXXWS3XKd2vc/TRBG9O5ELC17MmWubD2nKhUKZa26Ba2+D3P+4/MNCFwg59oWVeYhkzgN/JDR8deKBoD7Y+ljEjGZ0sosXVTvbc6RHirr2reNy1OXd6pJsQ+gqjk8VWFYmHrwBzW/n+uMPFiRwHB2I7ih8ciHFxIkd/3Omk5tCDV1t+2nNu5sxxpDFNx+huNhVT3/zMDz8usXC3ddaHBj1GHj/As08fwTS7Kt1HBTmyN29vdwAw+/wbwLVOJ3uAD1wi/dUH7Qei66PfyuRj4Ik9is+hglfbkbfR3cnZm7chlUWLdwmprtCohX4HUtlOcQjLYCu+fzGJH2QRKvP3UNz8bWk1qMxjGTOMThZ3kvgLI5AzFfo379UAAAAASUVORK5CYII=")

(def main-descriptor [{:label   "Type"
                       :key     :type
                       :type    :select
                       :options [{:key   :public-key
                                  :value "Public key"}
                                 {:key   :avatar
                                  :value "Avatar"}
                                 {:key   :group-avatar
                                  :value "Group avatar"}]}])

(defn cool-preview []
  (let [state (reagent/atom {:label "Name"
                             :type :group-avatar})]
    (fn []
      (let [contacts {example-pk {:public-key example-pk
                                  :names {:three-words-name "Automatic incompatible Coati"}
                                  :photo example-photo}
                      example-pk2 {:public-key example-pk2
                                   :names {:three-words-name "Clearcut Flickering Rattlesnake"}
                                   :photo example-photo2}}
            contacts-public-keys (map (fn [{:keys [public-key]}]
                                        {:key   public-key
                                         :value (multiaccounts/displayed-name
                                                 (get contacts public-key))})
                                      (vals contacts))
            current-username (if (seq (:contact @state))
                               (->> @state :contact contacts multiaccounts/displayed-name)
                               "Please select a user")
            descriptor
            (cond
              (= (:type @state) :group-avatar) (conj main-descriptor {:label "Label"
                                                                      :key   :label
                                                                      :type  :text})
              (= (:type @state) :avatar) (let [photo (-> @state :contact contacts :photo)]
                                           (when-not (contains? @state :contacts)
                                             (swap! state assoc :contacts contacts-public-keys))
                                           (when-not (= (:photo @state)
                                                        photo)
                                             (swap! state assoc :photo photo))
                                           (conj main-descriptor {:label   "Contacts"
                                                                  :key     :contact
                                                                  :type    :select
                                                                  :options contacts-public-keys})))]
        [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
         [rn/view {:padding-bottom 150}
          [rn/view {:flex 1}
           [preview/customizer state descriptor]]
          [rn/view {:padding-vertical 60
                    :flex-direction   :row
                    :justify-content  :center}
           (case (:type @state)
             :group-avatar
             [quo2/group-avatar-tag (:label @state) group-avatar-default-params]
             :public-key
             [quo2/public-key-tag {} example-pk]
             :avatar
             [quo2/user-avatar-tag {} current-username (:photo @state)])]]]))))

(defn preview-context-tags []
  [rn/view {:background-color (colors/theme-colors colors/white
                                                   colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
