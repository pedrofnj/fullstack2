<template>
  <v-container>
    <v-row class="align-center mb-4">
      <v-col cols="8"><h2>Minhas listas</h2></v-col>
      <v-col cols="4" class="text-right">
        <v-text-field v-model="newName" label="Nova lista" density="compact" hide-details
                      @keyup.enter="create" />
      </v-col>
    </v-row>

    <v-list>
      <v-list-item v-for="l in lists.items" :key="l.id" :to="{ name: 'tasks', params: { listId: l.id } }">
        <template #title>
          <div class="d-flex align-center justify-space-between">
            <div>{{ l.name }}</div>
            <div>
              <v-btn size="x-small" variant="text" @click.stop="rename(l)">Renomear</v-btn>
              <v-btn size="x-small" color="error" variant="text" @click.stop="remove(l.id)">Excluir</v-btn>
            </div>
          </div>
        </template>
      </v-list-item>
    </v-list>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useTasklistsStore } from '@/stores/useTasklistsStore'

const lists = useTasklistsStore()
const newName = ref('')

onMounted(lists.fetch)

const create = async () => {
  if (!newName.value.trim()) return
  await lists.create(newName.value.trim())
  newName.value = ''
}
const rename = async (l: { id: string; name: string }) => {
  const name = prompt('Novo nome:', l.name)
  if (name && name.trim() && name !== l.name) await lists.rename(l.id, name.trim())
}
const remove = async (id: string) => {
  if (confirm('Excluir esta lista e suas tarefas?')) await lists.remove(id)
}
</script>
