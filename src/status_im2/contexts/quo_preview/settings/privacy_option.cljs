(ns status-im2.contexts.quo-preview.settings.privacy-option
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Header:"
    :key   :header
    :type  :text}
   {:label "Footer:"
    :key   :footer
    :type  :text}
   {:label "Line 1:"
    :key   :li1
    :type  :text}
   {:label "Line 2:"
    :key   :li2
    :type  :text}
   {:label "Line 3:"
    :key   :li3
    :type  :text}])

(defn preview-options
  []
  (let [state (reagent/atom {:selected :contacts
                             :header   "header"
                             :footer   "footer"
                             :li1      "line item 1"
                             :li2      "line item 2"
                             :li3      "line item 3"})]
    (fn []
      (let [header     (:header @state)
            footer     (:footer @state)
            list-items (->> (select-keys @state [:li1 :li2 :li3])
                            vals
                            (remove empty?))]
        [preview/preview-container
         {:state      state
          :descriptor descriptor}
         [rn/view
          {:margin-horizontal 20
           :padding           16
           :flex              1}
          [rn/view {:margin-vertical 2}
           [quo/text {:size :paragraph-2} "Dynamic sample"]]
          [rn/view {:margin-vertical 8}
           [quo/privacy-option
            (cond-> {:on-select #(swap! state assoc :selected :preview)
                     :on-toggle #(js/alert "dynamic card toggled")
                     :active?   (= :preview (:selected @state))}
              (not-empty header)     (assoc :header header)
              (not-empty footer)     (assoc :footer footer)
              (not-empty list-items) (assoc :list-items list-items))]]

          [rn/view {:margin-vertical 2}
           [quo/text {:size :paragraph-2} "Static samples"]]
          [rn/view {:margin-vertical 8}
           [quo/privacy-option
            {:header     "Contacts only"
             :icon       :i/contact-book
             :on-select  #(swap! state assoc :selected :contacts)
             :active?    (= :contacts (:selected @state))
             :list-items ["Only add people from your contact list"
                          "Added members can add their own contacts"
                          "There is no link or QR for this group"
                          "No public information available"]}]]
          [rn/view {:margin-vertical 8}
           [quo/privacy-option
            {:icon       :i/world
             :header     "Anyone can request to join"
             :on-select  #(swap! state assoc :selected :anyone)
             :on-toggle  #(js/alert "card toggled")
             :active?    (= :anyone (:selected @state))
             :footer     "Members can approve join requests"
             :list-items ["Add people from your contact list"
                          "Invite prople from outside your contact list"
                          "Group is shareable via link"
                          "Group name and number of people is public"]}]]
          [rn/view {:margin-vertical 8}
           [quo/privacy-option
            {:icon :i/world
             :header "Sample card with very long text to test proper overflow behavior"
             :on-select #(swap! state assoc :selected :overflow)
             :on-toggle #(js/alert "card toggled")
             :active? (= :overflow (:selected @state))
             :footer "This footer is exceedingly long to test the overflowing behavior of text in it"
             :list-items
             ["A very, very very long text line that serves to test the overflow behavior of this component"]}]]]]))))
