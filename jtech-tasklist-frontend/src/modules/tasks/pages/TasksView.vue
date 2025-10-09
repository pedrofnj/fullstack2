<template>
  <v-container>
    <v-row class="align-center mb-4">
      <v-col cols="8"><h2>Tarefas</h2></v-col>
      <v-col cols="4" class="text-right">
        <v-text-field v-model="title" label="Nova tarefa" density="compact" hide-details
                      @keyup.enter="create" />
      </v-col>
    </v-row>

    <v-list>
      <v-list-item v-for="t in tasks.items" :key="t.id">
        <template #prepend>
          <v-checkbox-btn :model-value="t.completed" @click.stop="toggle(t)" />
        </template>
        <template #title>
          <div :style="{ textDecoration: t.completed ? 'line-through' : 'none' }">
            {{ t.title }}
          </div>
        </template>
        <template #append>
          <v-btn variant="text" size="x-small" @click="edit(t)">Editar</v-btn>
          <v-btn variant="text" size="x-small" color="error" @click="remove(t.id)">Excluir</v-btn>
        </template>
      </v-list-item>
    </v-list>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useTasksStore } from '@/stores/useTasksStore'
import { useRoute } from 'vue-router'

const tasks = useTasksStore()
const route = useRoute()
const title = ref('')

const load = async () => { await tasks.fetch(route.params.listId as string) }
onMounted(load)
watch(() => route.params.listId, load)

const create = async () => {
  if (!title.value.trim()) return
  await tasks.create(route.params.listId as string, title.value.trim())
  title.value = ''
}
const toggle = async (t: any) => { await tasks.toggle(t.id, !t.completed) }
const edit = async (t: any) => {
  const newTitle = prompt('Novo título:', t.title)
  if (newTitle && newTitle.trim()) await tasks.update({ ...t, title: newTitle.trim() })
}
const remove = async (id: string) => { if (confirm('Excluir tarefa?')) await tasks.remove(id) }
</script>
