<template>
  <f7-page :page-content="false">
    <f7-navbar>
      <f7-subnavbar>
        <f7-segmented raised>
          <f7-button>
            test
          </f7-button>
          <f7-button>
            test1
          </f7-button>
        </f7-segmented>
      </f7-subnavbar>
    </f7-navbar>
    <f7-list form class="page-content">
      <li>
        <div class="item-content item-input">
          <div class="item-inner">
            <div class="item-title item-label">Fruit</div>
            <div class="item-input-wrap">
              <label for="autocomplete-from-input"><input id="autocomplete-from-input" type="text" placeholder="Fruit"/></label>
            </div>
          </div>
        </div>
      </li>
    </f7-list>
  </f7-page>
</template>
<script>
export default {
  data () {
    console.info('about.....................')
    const cityItems = []
    for (let i = 0; i < 26; i++) {
      cityItems.push({
        id: i + 1,
        label: String.fromCharCode(65 + i) + '城市'
      })
      cityItems.push({
        id: i + 100,
        label: String.fromCharCode(65 + i) + 'City'
      })
    }
    return {
      cityItems
    }
  },
  mounted () {
    const self = this
    const app = self.$f7
    this.fromAutocomplete = app.autocomplete.create({
      // openerEl: '#autocomplete-from',
      inputEl: '#autocomplete-from-input',
      // openIn: 'popup',
      openIn: 'dropdown',
      valueProperty: 'id', // object's "value" property name
      textProperty: 'label',
      source: function (query, render) {
        let results = []
        if (query.length === 0) {
          render(results)
          return
        }
        for (let i = 0; i < self.cityItems.length; i++) {
          let cityItem = self.cityItems[i]
          if (cityItem.label.toLowerCase().indexOf(query.toLowerCase()) >= 0) results.push(cityItem)
        }
        render(results)
      },
      on: {
        change: function (value) {
          console.info(arguments)
          // Add item text value to item-after
          // self.$$('#autocomplete-from').find('.item-after').text(value[0])
          // Add item value to input value
          self.$$('#autocomplete-from').find('input.auto-value').val(value[0].id)
          self.$$('#autocomplete-from').find('input.auto-label').val(value[0].label)
        }
      }
    })
  },
  beforeDestroy () {
    this.fromAutocomplete.destroy()
  }
}
</script>
