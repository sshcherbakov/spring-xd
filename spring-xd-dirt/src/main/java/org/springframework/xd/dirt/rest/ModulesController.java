/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xd.dirt.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.xd.dirt.module.ModuleDefinitionService;
import org.springframework.xd.dirt.module.NoSuchModuleException;
import org.springframework.xd.module.ModuleDefinition;
import org.springframework.xd.module.ModuleType;
import org.springframework.xd.rest.domain.DetailedModuleDefinitionResource;
import org.springframework.xd.rest.domain.ModuleDefinitionResource;

/**
 * Handles all Module related interactions.
 *
 * @author Glenn Renfro
 * @author Mark Fisher
 * @author Gunnar Hillert
 * @author Eric Bottard
 */
@Controller
@RequestMapping("/modules")
@ExposesResourceFor(ModuleDefinitionResource.class)
public class ModulesController {

	private final Log logger = LogFactory.getLog(ModulesController.class);

	private final ModuleDefinitionService moduleDefinitionService;

	private final DetailedModuleDefinitionResourceAssembler detailedAssembler;

	private ModuleDefinitionResourceAssembler moduleDefinitionResourceAssembler = new ModuleDefinitionResourceAssembler();

	@Autowired
	public ModulesController(ModuleDefinitionService moduleDefinitionService,
			DetailedModuleDefinitionResourceAssembler detailedAssembler) {
		Assert.notNull(moduleDefinitionService, "moduleDefinitionService must not be null");
		Assert.notNull(detailedAssembler, "detailedAssembler must not be null");
		this.moduleDefinitionService = moduleDefinitionService;
		this.detailedAssembler = detailedAssembler;
	}

	/**
	 * List Module definitions.
	 */
	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public PagedResources<ModuleDefinitionResource> list(Pageable pageable,
			PagedResourcesAssembler<ModuleDefinition> assembler,
			@RequestParam(value = "type", required = false) ModuleType type) {
		Page<ModuleDefinition> page = type == null ? moduleDefinitionService.findDefinitions(pageable) : moduleDefinitionService.findDefinitions(pageable, type);
		PagedResources<ModuleDefinitionResource> result = assembler.toResource(page,
				new ModuleDefinitionResourceAssembler());
		return result;
	}

	/**
	 * Retrieve detailed module definition about a particular module.
	 */
	@RequestMapping(value = "/{type}/{name}", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public DetailedModuleDefinitionResource info(@PathVariable("type") ModuleType type,
			@PathVariable("name") String name) {
		ModuleDefinition def = moduleDefinitionService.findDefinition(name, type);
		if (def == null) {
			throw new NoSuchModuleException(name, type);
		}
		return detailedAssembler.toResource(def);
	}

	/**
	 * Create a new composite Module.
	 *
	 * @param name The name of the module to create (required)
	 * @param definition The module definition, expressed in the XD DSL (required)
	 */
	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public ModuleDefinitionResource save(@RequestParam("name") String name,
			@RequestParam("definition") String definition) {
		ModuleDefinition moduleDefinition = moduleDefinitionService.compose(name, /*TODO*/null, definition);
		ModuleDefinitionResource resource = moduleDefinitionResourceAssembler.toResource(moduleDefinition);
		return resource;
	}

	/**
	 * Delete a module.
	 */
	@RequestMapping(value = "/{type}/{name}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	public void delete(@PathVariable("type") ModuleType type, @PathVariable("name") String name) {
		moduleDefinitionService.delete(name, type);
	}

}
