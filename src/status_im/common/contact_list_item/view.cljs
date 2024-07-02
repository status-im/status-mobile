(ns status-im.common.contact-list-item.view
  (:require
    [quo.core :as quo]
    [utils.address :as address]
    [utils.re-frame :as rf]))

(defn contact-list-item
  [{:keys [on-press on-long-press accessory allow-multiple-presses? disabled?]}
   {:keys [primary-name secondary-name public-key compressed-key ens-verified added?
           container-style]}
   theme]
  (let [photo-path          (rf/sub [:chats/photo-path public-key])
        online?             (rf/sub [:visibility-status-updates/online? public-key])
        customization-color (rf/sub [:profile/customization-color])]
    [quo/user
     {:customization-color     customization-color
      :allow-multiple-presses? allow-multiple-presses?
      :theme                   theme
      :short-chat-key          (address/get-shortened-compressed-key (or compressed-key public-key))
      :primary-name            primary-name
      :secondary-name          secondary-name
      :photo-path              photo-path
      :online?                 online?
      :verified?               ens-verified
      :contact?                added?
      :on-press                on-press
      :on-long-press           on-long-press
      :accessory               accessory
      :container-style         container-style
      :disabled?               disabled?}]))
