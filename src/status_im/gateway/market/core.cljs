(ns status-im.gateway.market.core)

(defn page-size-to-ensure-tokens-updating
  "Calculates the optimal page size to ensure that tokens are updated 
   based on the current visible index and the count of visible items.
   
   Takes a map with keys:
   - :first-visible-index (int) - the index of the first visible item
   - :visible-count (int) - the number of items currently visible

   Returns a map containing:
   - :page-size (int) - the calculated size of the page
   - :page-index (int) - the index of the computed page (1-based)

   If no valid page size is found, returns nil.
   
   Example usage:
   (page-size-to-ensure-tokens-updating {:first-visible-index 14
                                          :visible-count       11})"

  [{:keys [first-visible-index visible-count]}]
  (let [from          first-visible-index
        to            (+ first-visible-index visible-count -1)
        min-page-size visible-count
        max-page-size (inc to)]
    (when (pos? visible-count)
      (some (fn [page-size]
              (let [page-index (quot from page-size)
                    page-start (* page-index page-size)
                    page-end   (+ page-start page-size -1)]
                (when (and (<= page-start from)
                           (>= page-end to))
                  {:page-size  page-size
                   :page-index (inc page-index)})))
            (range min-page-size (inc max-page-size))))))

